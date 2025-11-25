package elf.sections;

import elf.Elf;
import elf.exceptions.ElfException;
import elf.sections.symbol.Symbol;
import elf.word.Word;

import java.util.List;

public class SymbolTable<T extends Word> extends ElfSection<T>{

    public SymbolTable(Elf<T> elf) throws ElfException {
        super(elf);
    }
}
