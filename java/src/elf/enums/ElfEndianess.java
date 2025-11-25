package elf.enums;

import elf.exceptions.InvalidElfEndianess;

public enum ElfEndianess {
    Big(0x02),
    Little(0x01);

    public final byte repr;
    ElfEndianess(int repr){
        this.repr = (byte)repr;
    }

    public static ElfEndianess from_repr(byte repr) throws InvalidElfEndianess {
        return switch (repr) {
            case 1 -> ElfEndianess.Little;
            case 2 -> ElfEndianess.Big;
            default -> throw new InvalidElfEndianess(repr);
        };
    }
}
