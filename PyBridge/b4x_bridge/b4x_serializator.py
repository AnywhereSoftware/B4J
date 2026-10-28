import struct
import zlib
from typing import Optional, Any, Callable, Type
import io

# version: 1.01

class B4XSerializator:
    _T_NULL = 0
    _T_STRING = 1
    _T_SHORT = 2
    _T_INT = 3
    _T_LONG = 4
    _T_FLOAT = 5
    _T_DOUBLE = 6
    _T_BOOLEAN = 7
    _T_BYTE = 10
    _T_CHAR = 14
    _T_MAP = 20
    _T_LIST = 21
    _T_NSARRAY = 22
    _T_NSDATA = 23
    _T_TYPE = 24
    _BYTES_TO_NUMBERS = {_T_SHORT: ("h", 2), _T_INT: ("i", 4), _T_LONG: ("q", 8),
                         _T_FLOAT: ("f", 4), _T_DOUBLE: ("d", 8), _T_BYTE: ("b", 1),
                         _T_CHAR: ("H", 2)}

    def __init__(self, types: Optional[list[type]] = None, prefer_doubles: bool = True):
        """

        :param types: list of types that can be instantiated during deserialization.
        :param prefer_doubles: whether to store floating point numbers as doubles. True by default.
        """
        self._index = 0
        self._mem: Optional[memoryview] = None
        self._writer: Optional[io.BytesIO] = None
        if types is None:
            types = []
        self._types = {k.__name__.lower(): k for k in types}
        self.prefer_doubles = prefer_doubles
        self.converters: dict[type, Callable] = {}

    def add_type(self, _type):
        self._types[_type.__name__.lower()] = _type

    def convert_object_to_bytes(self, obj: Any) -> bytes:
        """
        Serializes a DataClass object to bytes. See B4X documentation for supported types.
        Note that float types will be stored as double or float based on the value of prefer_doubles.
        Int numbers will be stored as ints if possible and longs for larger numbers.
        :param obj:
        :return:
        """
        self._writer = io.BytesIO()
        self._write_object(obj)
        b = zlib.compress(self._writer.getvalue())
        self._writer.close()
        return b

    def is_serializable(self, obj: Any) -> Optional[Type]:
        if obj is None or isinstance(obj, (bool, int, float, str, bytes, bytearray)):
            return None
        if isinstance(obj, (list, tuple)):
            for item in obj:
                problem = self.is_serializable(item)
                if problem is not None: return problem
            return None
        elif isinstance(obj, dict):
            for key, value in obj.items():
                problem = self.is_serializable(key)
                if problem is not None: return problem
                problem = self.is_serializable(value)
                if problem is not None: return problem
            return None
        elif type(obj) in self.converters:
            return None
        elif type(obj).__name__.lower() in self._types:
            return None
        else:
            return type(obj)

    def _write_object(self, obj) -> None:
        if obj is None:
            self._write_byte(self._T_NULL)
        elif isinstance(obj, bool):
            self._write_byte(self._T_BOOLEAN)
            self._write_byte(1 if obj is True else 0)
        elif isinstance(obj, int):
            if 0x7FFFFFFF >= obj >= -0x80000000:
                self._write_byte(self._T_INT)
                self._write_int(obj)
            else:
                self._write_byte(self._T_LONG)
                self._writer.write(struct.pack("<q", obj))
        elif isinstance(obj, float):
            if self.prefer_doubles:
                self._write_byte(self._T_DOUBLE)
                self._writer.write(struct.pack("<d", obj))
            else:
                self._write_byte(self._T_FLOAT)
                self._writer.write(struct.pack("<f", obj))
        elif isinstance(obj, str):
            self._write_byte(self._T_STRING)
            b = obj.encode("utf-8")
            self._write_int(len(b))
            self._writer.write(b)
        elif isinstance(obj, list):
            self._write_byte(self._T_LIST)
            self._write_list(obj)
        elif isinstance(obj, dict):
            self._write_byte(self._T_MAP)
            self._write_map(obj)
        elif isinstance(obj, (bytes, bytearray)):
            self._write_byte(self._T_NSDATA)
            self._write_int(len(obj))
            self._writer.write(obj)
        elif isinstance(obj, tuple):
            self._write_byte(self._T_NSARRAY)
            self._write_list(list(obj))
        elif type(obj) in self.converters:
            self._write_object(self.converters[type(obj)](obj))
        else:
            self._write_byte(self._T_TYPE)
            self._write_type(obj)

    def _write_type(self, obj):
        t = type(obj)
        name: str = t.__name__
        self._write_object("_" + name.lower())
        map1 = {}
        for field in t.__dataclass_fields__.keys():
            map1[field] = getattr(obj, field)
        self._write_map(map1)

    def _write_map(self, map1: dict):
        self._write_int(len(map1))
        for k, v in map1.items():
            self._write_object(k)
            self._write_object(v)

    def _write_list(self, lst: list):
        self._write_int(len(lst))
        for o in lst:
            self._write_object(o)

    def _write_int(self, i: int):
        self._writer.write(struct.pack("<i", i))

    def _write_byte(self, byte: int):
        self._writer.write(bytes([byte]))

    def convert_bytes_to_object(self, data: bytes) -> Any:
        """
        Converts an array of bytes, serialized with B4XSerializator to an object.
        :param data: bytes array
        :return: The deserialized object.
        """
        inflated = zlib.decompress(data)
        self._mem = memoryview(inflated)
        self._index = 0
        res = self._read_object()
        self._mem.release()
        return res

    def _read_object(self) -> Any:
        t: int = self._read_bytes(1)[0]
        if t == self._T_NULL:
            return None
        elif t in self._BYTES_TO_NUMBERS:
            fmt, length = self._BYTES_TO_NUMBERS[t]
            return struct.unpack("<" + fmt, self._read_bytes(length))[0]
        elif t == self._T_BOOLEAN:
            return self._read_bytes(1)[0] == 1
        elif t == self._T_STRING:
            b = self._read_bytes(self._read_int())
            return b.tobytes().decode("utf-8")
        elif t == self._T_LIST:
            return self._read_list()
        elif t == self._T_MAP:
            return self._read_map()
        elif t == self._T_NSDATA:
            length = self._read_int()
            return self._read_bytes(length).tobytes()
        elif t == self._T_NSARRAY:
            return tuple(self._read_list())
        elif t == self._T_TYPE:
            return self._read_type()

    def _read_type(self) -> Any:
        cls = self._read_object()
        map1 = self._read_map()
        map1 = {k[1:] if k.startswith("_") else k: v for k, v in map1.items()}
        typ = self._fix_cls_name(cls)
        # noinspection PyUnresolvedReferences
        fields = typ.__dataclass_fields__.keys()
        map1 = {k: v for k, v in map1.items() if k in fields}
        obj = typ(**map1)
        return obj

    def _fix_cls_name(self, cls: str) -> type:
        i = cls.find("$")
        if i > -1:
            cls = cls[i + 1:]
        return self._types[cls[1:]]

    def _read_map(self) -> dict:
        length = self._read_int()
        res = {}
        for _ in range(length):
            key = self._read_object()
            value = self._read_object()
            res[key] = value
        return res

    def _read_list(self) -> list:
        length = self._read_int()
        res = []
        for _ in range(length):
            res.append(self._read_object())
        return res

    def _read_int(self) -> int:
        return struct.unpack("<i", self._read_bytes(4))[0]

    def _read_bytes(self, length: int) -> memoryview:
        res = self._mem[self._index: self._index + length]
        self._index += length
        return res
