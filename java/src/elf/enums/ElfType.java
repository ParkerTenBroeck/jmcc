package elf.enums;

import elf.exceptions.InvalidElfType;

public enum ElfType {
    Unknown(0x00),
    Relocation(0x01),
    Executable(0x02),
    SharedObject(0x03),
    Core(0x04);

    public final short repr;
    ElfType(int repr){
        this.repr = (short)repr;
    }

    public static ElfType from_repr(short repr) throws InvalidElfType {
        return switch (repr) {
            case 0 -> ElfType.Unknown;
            case 1 -> ElfType.Relocation;
            case 2 -> ElfType.Executable;
            case 3 -> ElfType.SharedObject;
            case 4 -> ElfType.Core;
            default -> throw new InvalidElfType(repr);
        };
    }
}
