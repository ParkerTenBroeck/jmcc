package elf.enums;


public sealed interface ElfMachine{
    enum KnownElfMachine implements ElfMachine {
        Mips(0x08),
        Arm(0x28),
        ZilogZ80(0xDC),
        Riscv(0xF3);

        public final short repr;

        KnownElfMachine(int repr) {
            this.repr = (short) repr;
        }
    }

    record UnknownElfMachine(short repr) implements ElfMachine{}

    static ElfMachine from_repr(short repr) {
        return switch (repr) {
            case 0x08 -> KnownElfMachine.Mips;
            case 0x28 -> KnownElfMachine.Arm;
            case 0xDC -> KnownElfMachine.ZilogZ80;
            case 0xF3 -> KnownElfMachine.Riscv;
            default -> new UnknownElfMachine(repr);
        };
    }

}
