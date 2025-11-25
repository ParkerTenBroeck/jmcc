package elf.exceptions;

public class InvalidElfAbi extends ElfException {
    public InvalidElfAbi(byte abi){
        super(String.format("%02X", abi));
    }
}
