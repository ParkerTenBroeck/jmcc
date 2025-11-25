package elf;

import elf.enums.*;
import elf.exceptions.*;
import elf.programs.ElfProgram;
import elf.sections.ElfSection;
import elf.sections.StringSection;
import elf.sections.SymbolTable;
import elf.word.W32;
import elf.word.W64;
import elf.word.Word;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Elf<T extends Word> {
    public final ByteBuffer src;

    public final ElfClass elf_class;
    public final ElfEndianess endianess;
    public final ElfAbi abi;
    public final byte abi_version;
    public final ElfMachine machine;
    public final ElfType type;
    public final T entry;
    public final T program_header_offset;
    public final T section_header_offset;
    public final int flags;
    public final short elf_header_size;
    public final short program_header_entry_size;
    public final short program_header_number;
    public final short section_header_entry_size;
    public final short section_header_number;
    public final short section_header_str_table_index;

    public final List<ElfSection<T>> sections;
    public final List<ElfProgram<T>> programs;

    protected HashMap<ElfSectionType, ElfSectionLoader<T>> sectionLoaderMap = new HashMap<>();

    {
        sectionLoaderMap.put(ElfSectionType.KnownSectionType.NULL, ElfSection::new);
        sectionLoaderMap.put(ElfSectionType.KnownSectionType.STRTAB, StringSection::new);
        sectionLoaderMap.put(ElfSectionType.KnownSectionType.SYMTAB, SymbolTable::new);
    }

    public sealed interface ElfLoadKind{}
    public record Elf32(Elf<W32> elf) implements ElfLoadKind {}
    public record Elf64(Elf<W64> elf) implements ElfLoadKind {}
    public interface ElfLoader{
        Elf<?> load(ByteBuffer buffer) throws ElfException;
    }
    public interface ElfSectionLoader<T extends Word>{
        ElfSection<T> load(Elf<T> elf) throws ElfException;
    }

    public static ElfLoadKind load(Path path) throws IOException, ElfException {
        return load(path, Elf::new);
    }

    @SuppressWarnings("unchecked")
    public static ElfLoadKind load(Path path, ElfLoader loader) throws IOException, ElfException {
        try (var channel = FileChannel.open(path, StandardOpenOption.READ)) {
            var map = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            Elf<?> elf = loader.load(map);
            return switch (elf.elf_class){
                case ElfClass.S32 -> new Elf32((Elf<W32>) elf);
                case ElfClass.S64 -> new Elf64((Elf<W64>) elf);
            };
        }
    }

    protected Elf(ByteBuffer buffer) throws ElfException {
        this.src = buffer;
        this.src.position(0);

        if (this.src.getInt() != 0x7F454C46) throw new InvalidElfMagicNumber(this.src.getInt(0));

        this.elf_class = ElfClass.from_repr(src.get());
        this.endianess = ElfEndianess.from_repr(src.get());
        switch (this.endianess) {
            case Big -> this.src.order(ByteOrder.BIG_ENDIAN);
            case Little -> this.src.order(ByteOrder.LITTLE_ENDIAN);
        }

        if (this.src.get() != 1) throw new InvalidElfVersion(this.src.get(0x6));

        this.abi = ElfAbi.from_repr(buffer.get());
        this.abi_version = buffer.get();

        for (int i = 0; i < 7; i++) buffer.get();//padding

        this.type = ElfType.from_repr(buffer.getShort());
        this.machine = ElfMachine.from_repr(buffer.getShort());
        if (buffer.getInt() != 1) throw new InvalidElfVersion(this.src.get(0x14));

        this.entry = this.getWord();
        this.program_header_offset = this.getWord();
        this.section_header_offset = this.getWord();
        this.flags = buffer.getInt();
        this.elf_header_size = buffer.getShort();
        this.program_header_entry_size = buffer.getShort();
        this.program_header_number = buffer.getShort();
        this.section_header_entry_size = buffer.getShort();
        this.section_header_number = buffer.getShort();
        this.section_header_str_table_index = buffer.getShort();

        {
            var list = new ArrayList<ElfSection<T>>();
            for (int i = 0; i < this.section_header_number; i++) {
                this.src.position((int)this.section_header_offset.long_value()+i*this.section_header_entry_size);
                list.add(this.load_section());
            }
            this.sections = List.copyOf(list);
        }

        {
            var list = new ArrayList<ElfProgram<T>>();
            for(int i = 0; i < this.program_header_number; i ++){
                this.src.position((int)this.program_header_offset.long_value()+i*this.program_header_entry_size);
                list.add(load_program());
            }
            this.programs = List.copyOf(list);
        }
    }

    protected ElfProgram<T> load_program() throws ElfException {
        return new ElfProgram<>(this);
    }

    protected ElfSection<T> load_section() throws ElfException {
        var type = ElfSectionType.from_repr(this.src.getInt(this.src.position()+4));
        return sectionLoaderMap.getOrDefault(type, ElfSection::new).load(this);
    }


    public StringSection<T> str_table(){
        return this.str_table(this.section_header_str_table_index);
    }

    public StringSection<T> str_table(int section){
        return (StringSection<T>) this.sections.get(section);
    }

    @SuppressWarnings("unchecked")
    public T getWord() {
        if (this.elf_class == ElfClass.S32) {
            return (T)new W32(src.getInt());
        } else {
            return (T)new W64(src.getLong());
        }
    }

    @Override
    public String toString() {
        return "Elf32{" +
                "\n\tentry: " + entry.to_str_hex() +
                ", \n\telf_class: " + elf_class +
                ", \n\tendianess: " + endianess +
                ", \n\tabi: " + abi +
                ", \n\tabi_version: " + abi_version +
                ", \n\tmachine: " + machine +
                ", \n\ttype: " + type +
                ", \n\tprogram_header_offset: " + program_header_offset.to_str_hex() +
                ", \n\tsection_header_offset: " + section_header_offset.to_str_hex() +
                ", \n\tflags: " + String.format("0x%08X", flags) +
                ", \n\telf_header_size: " + String.format("0x%04X", elf_header_size) +
                ", \n\tprogram_header_entry_size: " + String.format("0x%04X", program_header_entry_size) +
                ", \n\tsection_header_number: " + program_header_number +
                ", \n\tsection_header_entry_size: " + String.format("0x%04X", section_header_entry_size) +
                ", \n\tsection_header_number: " + section_header_number +
                ", \n\tsection_header_str_table_index: " + section_header_str_table_index +
                ", \n\tprograms: [" + String.join(",\n", programs.stream().map(Object::toString).toList()) + "\n\t]" +
                ", \n\tsections: [" + String.join(",\n", sections.stream().map(Object::toString).toList()) + "\n\t]" +
                "\n}";
    }
}
