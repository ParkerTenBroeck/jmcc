package elf.enums;

import elf.exceptions.InvalidElfClass;

public enum ElfClass {
    S32(0x01),
    S64(0x02);

    public final byte repr;
    ElfClass(int repr){
        this.repr = (byte)repr;
    }

    public static ElfClass from_repr(byte repr) throws InvalidElfClass {
        return switch (repr) {
            case 1 -> ElfClass.S32;
            case 2 -> ElfClass.S64;
            default -> throw new InvalidElfClass(repr);
        };
    }
}
