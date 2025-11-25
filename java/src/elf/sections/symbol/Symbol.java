package elf.sections.symbol;

import elf.Elf;
import elf.word.Word;

public class Symbol<T extends Word> {
    public final int name;
    public final Word value;
    public final Word size;
    public final byte info;
    public final byte other;
    public final short shndx;

    public Symbol(Elf<T> elf){
        switch(elf.elf_class){
            case S32 -> {
                this.name = elf.src.getInt();
                this.value = elf.getWord();
                this.size = elf.getWord();
                this.info = elf.src.get();
                this.other = elf.src.get();
                this.shndx = elf.src.getShort();
            }
            case S64 -> {
                this.name = elf.src.getInt();
                this.info = elf.src.get();
                this.other = elf.src.get();
                this.shndx = elf.src.getShort();
                this.value = elf.getWord();
                this.size = elf.getWord();
            }
            default -> throw new RuntimeException();
        }
    }
}
