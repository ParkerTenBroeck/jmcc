package elf.enums;

public sealed interface ElfSectionType {
    int repr();

    record OperatingSystemSpecificSectionType(int repr) implements ElfSectionType {
    }

    enum KnownSectionType implements ElfSectionType {
        NULL(0x0),
        PROGBITS(0x1),
        SYMTAB(0x2),
        STRTAB(0x3),
        RELA(0x4),
        HASH(0x5),
        DYNAMIC(0x6),
        NOTE(0x7),
        NOBITS(0x8),
        REL(0x9),
        SHLIB(0x0A),
        DYNSYM(0x0B),
        INIT_ARRAY(0x0E),
        FINI_ARRAY(0x0F),
        PREINIT_ARRAY(0x10),
        GROUP(0x11),
        SYMTAB_SHNDX(0x12),
        NUM(0x13);

        public final int repr;
        public int repr(){
            return this.repr;
        }
        KnownSectionType(int repr) {
            this.repr = repr;
        }
    }

    static ElfSectionType from_repr(int repr) {
        return switch (repr) {
            case 0x0 -> KnownSectionType.NULL;
            case 0x1 -> KnownSectionType.PROGBITS;
            case 0x2 -> KnownSectionType.SYMTAB;
            case 0x3 -> KnownSectionType.STRTAB;
            case 0x4 -> KnownSectionType.RELA;
            case 0x5 -> KnownSectionType.HASH;
            case 0x6 -> KnownSectionType.DYNAMIC;
            case 0x7 -> KnownSectionType.NOTE;
            case 0x8 -> KnownSectionType.NOBITS;
            case 0x9 -> KnownSectionType.REL;
            case 0x0A -> KnownSectionType.SHLIB;
            case 0x0B -> KnownSectionType.DYNSYM;
            case 0x0E -> KnownSectionType.INIT_ARRAY;
            case 0x0F -> KnownSectionType.FINI_ARRAY;
            case 0x10 -> KnownSectionType.PREINIT_ARRAY;
            case 0x11 -> KnownSectionType.GROUP;
            case 0x12 -> KnownSectionType.SYMTAB_SHNDX;
            case 0x13 -> KnownSectionType.NUM;
            default -> new OperatingSystemSpecificSectionType(repr);
        };
    }
}
