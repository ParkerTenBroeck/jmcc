package elf.enums;

import elf.exceptions.InvalidElfProgramType;

public sealed interface ElfProgramType {
    int repr();

    enum KnownProgramType implements ElfProgramType {
        NULL(0x00000000),
        LOAD(0x00000001),
        DYNAMIC(0x00000002),
        INTERP(0x00000003),
        NOTE(0x00000004),
        SHLIB(0x00000005),
        PHDR(0x00000006),
        TLS(0x00000007);

        public final int repr;
        public int repr(){
            return this.repr;
        }

        KnownProgramType(int repr){
            this.repr = repr;
        }
    }

    record OperatingSystemSpecificProgramType(int repr) implements ElfProgramType { }
    record ProcessorSpecificProgramType(int repr) implements ElfProgramType {}


    static ElfProgramType from_repr(int repr) throws InvalidElfProgramType {
        return switch(repr){
            case 0x00000000 -> KnownProgramType.NULL;
            case 0x00000001 -> KnownProgramType.LOAD;
            case 0x00000002 -> KnownProgramType.DYNAMIC;
            case 0x00000003 -> KnownProgramType.INTERP;
            case 0x00000004 -> KnownProgramType.NOTE;
            case 0x00000005 -> KnownProgramType.SHLIB;
            case 0x00000006 -> KnownProgramType.PHDR;
            case 0x00000007 -> KnownProgramType.TLS;
            case int i when i >= 0x60000000 && i <= 0x6FFFFFFF -> new OperatingSystemSpecificProgramType(repr);
            case int i when i >= 0x70000000 && i <= 0x7FFFFFFF -> new ProcessorSpecificProgramType(repr);
            default -> throw new InvalidElfProgramType(repr);
        };
    }
}
