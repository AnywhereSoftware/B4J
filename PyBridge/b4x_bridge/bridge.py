import asyncio
import builtins
import importlib
import sys
from dataclasses import dataclass
from enum import IntEnum
from typing import Tuple, List, Optional, Iterable
from b4x_bridge.b4x_serializator import B4XSerializator


@dataclass(frozen=True, slots=True)
class PyObject:
    Key: int

class TaskType(IntEnum):
    RUN = 1
    GET = 2
    RUN_ASYNC = 3
    CLEAN = 4
    ERROR = 5
    EVENT = 6
    PING = 7
    FLUSH = 8


@dataclass(slots=True, frozen=True)
class Task:
    id: int
    task_type: TaskType
    extra: list
    def to_tuple(self) -> Tuple:
        return self.id, self.task_type.value, self.extra

    def get_store_key(self) -> int:
        return self.extra[4]

    @classmethod
    def return_value_task (cls, _id: int, value, serializator: B4XSerializator) -> "Task":
        task_type = TaskType.RUN
        if isinstance(value, Exception):
            task_type = TaskType.ERROR
            value = str(value)
        problematic_type = serializator.is_serializable(value)
        if problematic_type:
            value = f"Unserializable object ({problematic_type.__name__})"
        return Task(id=_id, task_type=task_type, extra=[value])



class B4XBridge:
    def __init__(self, port:int, watchdog:int):
        self.memory:dict[int, object] = {
            1: self,
            2: importlib,
            3: builtins
        }
        from .comm_manager import CommManager
        self.comm = CommManager(self, port)
        self.version = "1.00"
        self.max_time_until_pong = watchdog
        self.python_objects = {}
        self.python_objects_counter = 0

    def store_python_object(self, obj) -> int:
        self.python_objects_counter += 1
        self.python_objects[self.python_objects_counter] = obj
        return self.python_objects_counter

    def get_and_remove_object(self, counter) -> object:
        return self.python_objects.pop(counter)

    def register_dataclass(self, cls):
        self.comm.serializator.add_type(cls)

    async def start(self):
        await self.comm.connect()

    def unwrap_list(self, _list: List, copy: bool) -> list:
        if copy:
            _list = list(_list)
        for i in range(len(_list)):
            obj = _list[i]
            if isinstance(obj, PyObject):
                _list[i] = self.memory[obj.Key]
            elif isinstance(obj, dict):
                _list[i] = self.unwrap_dict(obj, copy)
            elif isinstance(obj, list):
                _list[i] = self.unwrap_list(obj, copy)
            elif isinstance(obj, tuple):
                _list[i] = self.unwrap_tuple(obj)
        return _list


    def unwrap_dict(self, _dict: dict, copy: bool) -> dict:
        if copy:
            _dict = dict(_dict)
        for k, v in _dict.items():
            if isinstance(v, PyObject):
                _dict[k] = self.memory[v.Key]
            elif isinstance(v, dict):
                _dict[k] = self.unwrap_dict(v, copy)
            elif isinstance(v, list):
                _dict[k] = self.unwrap_list(v, copy)
            elif isinstance(v, tuple):
                _dict[k] = self.unwrap_tuple(v)
        return _dict

    def to_array(self, iterable:Iterable, start:int, count: int):
        self.memory[start] = None
        for ix, obj in enumerate(iterable):
            if ix >= count:
                break
            self.memory[start+ix] = obj
        last_key = start + count - 1
        if last_key not in self.memory:
            raise IndexError()
        return self.memory[last_key]

    def unwrap_tuple(self, _tuple: tuple) -> tuple:
        _list = list(_tuple)
        self.unwrap_list(_list, copy=False)
        return tuple(_list)

    def run(self, task: Task, copy_args: bool) -> Tuple[bool, Optional[Exception]]:
        target, method_name, args, kwargs, store_target, *line_information = task.extra
        target = self.memory[target]
        args = self.unwrap_list(args, copy_args)
        kwargs = self.unwrap_dict(kwargs, copy_args)
        try:
            method = getattr(target, method_name)
            res = method(*args, **kwargs)
            self.memory[store_target] = res
            return True, None
        except Exception as e:
            e = self.wrap_exception(target, method_name, e, line_information)
            self.memory[store_target] = e
            return False, e

    async def run_async(self, task: Task, error: Optional[Exception]):
        target, method_name, args, kwargs, store_target, *line_information = task.extra
        self.unwrap_list(args, False)
        self.unwrap_dict(kwargs, False)
        if error is not None:
            res = error
        else:
            try:
                target = self.memory[target]
                method = getattr(target, task.extra[1])
                res = await method(*args, **kwargs)
            except Exception as e:
                res = self.wrap_exception(target, task.extra[1], e, line_information)
        self.memory[store_target] = res
        self.comm.write_queue.put_nowait(Task.return_value_task(task.id, res, self.comm.serializator))

    def raise_event(self, event_name: str, params: dict):
        if params:
            self.unwrap_dict(params, False)
        problematic_type = self.comm.serializator.is_serializable(params)
        if problematic_type:
            print(f"Event {event_name} cancelled. Unserializable object ({problematic_type.__name__})")
            return
        task = Task(0, TaskType.EVENT, [event_name, params])
        if self.comm.write_queue.empty():
            self.comm.write_task(task)
        else:
            self.comm.write_queue.put_nowait(task)

    def get_pyobject(self, task: Task, error: Optional[Exception]):
        object_id = task.extra[0]
        if error is not None:
            obj = error
        else:
            obj = self.memory[object_id]
        self.comm.write_queue.put_nowait(Task.return_value_task(task.id, obj, self.comm.serializator))

    def clean(self, task: Task):
        for key in task.extra:
            if key in self.memory:
                del self.memory[key]

    def wrap_exception(self, target, method, exception, line_information:list) -> Exception:
        msg = f"Python Error ({type(exception).__name__}) - Method: {type(target).__name__}.{method}: {exception}"
        if line_information and len(line_information[0]) > 0:
            msg = f"({line_information[0]}.{line_information[1]}) - {msg}"
        e = Exception(msg)
        if not isinstance(exception, StopIteration):
            if len(line_information) == 3 and line_information[2] > 0:
                print("~de:" + line_information[0] + "," + str(line_information[2]), file=sys.stderr)
            print(e, file=sys.stderr)
        return e

    async def wait_for_incoming(self):
        while True:
            tasks: List[Task] = await self.comm.read_queue.get()
            self.run_tasks(tasks, False)

    def run_tasks(self, tasks: List[Task], copy_args: bool):
        state_good = True
        e: Optional[Exception] = None
        for task in tasks:
            # print(task)
            if task.task_type == TaskType.RUN:
                if not state_good:
                    self.memory[task.get_store_key()] = e
                else:
                    state_good, e = self.run(task, copy_args=copy_args)
            elif task.task_type == TaskType.GET:
                self.get_pyobject(task, e)
            elif task.task_type == TaskType.RUN_ASYNC:
                asyncio.create_task(self.run_async(task, e))
            elif task.task_type == TaskType.CLEAN:
                self.clean(task)
            elif task.task_type == TaskType.FLUSH:
                if not state_good:
                    task = Task(id=task.id, task_type=TaskType.ERROR, extra=[str(e)])
                self.comm.write_queue.put_nowait(task)


    def kill(self):
        sys.exit()

def task_done_callback(task: asyncio.Task):
    try:
        task.result()
    except Exception as e:
        print(f"Unhandled exception in task: {repr(e)}", file=sys.stderr)
        sys.exit()

def exception_handler(_, context):
    print(context, file=sys.stderr)

bridge_instance = None
async def main():
    port = int(sys.argv[1])
    watchdog = int(sys.argv[2])
    global bridge_instance
    bridge_instance = B4XBridge(port, watchdog)
    print(f"starting PyBridge v{bridge_instance.version}")
    print(f"watchdog set to {watchdog} seconds" if watchdog else "watchdog disabled")
    loop = asyncio.get_event_loop()
    loop.set_exception_handler(exception_handler)
    await bridge_instance.start()
    await bridge_instance.wait_for_incoming()




