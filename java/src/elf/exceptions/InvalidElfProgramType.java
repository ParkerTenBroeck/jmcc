package elf.exceptions;

public class InvalidElfProgramType extends ElfException {
    public InvalidElfProgramType(int repr) {
        super(String.format("0x%08X", repr));
    }
}
