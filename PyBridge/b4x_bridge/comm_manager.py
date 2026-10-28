import asyncio
import sys
from asyncio import Queue, StreamReader, StreamWriter
from typing import List

from b4x_bridge.b4x_serializator import B4XSerializator
from b4x_bridge.bridge import B4XBridge, Task, PyObject, TaskType, task_done_callback
import threading

class CommManager:
    def __init__(self, bridge: B4XBridge, port: int):
        self.bridge = bridge
        self.serializator = B4XSerializator(types=[Task, PyObject])
        self.port = port
        self.read_queue: Queue[List[Task]] = Queue()
        self.write_queue: Queue[Task] = Queue()
        self.writer = None
        self.number_of_pings = 0
        self.write_lock = threading.Lock()

    async def connect(self):
        print("Connecting to port: {}".format(self.port))
        reader, writer = await asyncio.open_connection("127.0.0.1", self.port)
        self.writer = writer
        asyncio.create_task(self.reader_loop(reader)).add_done_callback(task_done_callback)
        asyncio.create_task(self.writer_loop()).add_done_callback(task_done_callback)
        asyncio.create_task(self.heartbeat()).add_done_callback(task_done_callback)

    async def heartbeat(self):
        while True:
            await asyncio.sleep(1)
            if self.bridge.max_time_until_pong > 0:
                self.write_queue.put_nowait(Task(task_type=TaskType.PING, id=0, extra=[]))
                if self.number_of_pings > self.bridge.max_time_until_pong:
                    sys.exit()
                self.number_of_pings += 1



    async def reader_loop(self, reader: StreamReader):
        while True:
            length: bytes = await reader.readexactly(4)
            n = int.from_bytes(length, 'big')
            data = await reader.readexactly(n)
            flat_tasks: List = self.serializator.convert_bytes_to_object(data)
            tasks = []
            for i in range(0, len(flat_tasks), 3):
                task = Task(id=flat_tasks[i], task_type=TaskType(flat_tasks[i + 1]), extra=flat_tasks[i + 2])
                if task.task_type == TaskType.PING:
                    self.number_of_pings = 0
                else:
                    tasks.append(task)
            self.read_queue.put_nowait(tasks)

    async def writer_loop(self):
        while True:
            task = await self.write_queue.get()
            self.write_task(task)

    def write_task(self, task: Task):
        with self.write_lock:
            data = self.serializator.convert_object_to_bytes(task.to_tuple())
            prefix: bytes = len(data).to_bytes(length=4, byteorder='big')
            self.writer.write(prefix)
            self.writer.write(data)

    async def close(self):
        if self.writer:
            self.writer.close()
            await self.writer.wait_closed()
