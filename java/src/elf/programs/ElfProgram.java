package elf.programs;

import elf.Elf;
import elf.enums.ElfProgramType;
import elf.exceptions.ElfException;
import elf.flags.ProgramFlags;
import elf.sections.ElfSection;
import elf.word.Word;

import java.util.List;

public class ElfProgram<T extends Word> {
    public final Elf<T> parent;

    /**
     * Identifies the type of the segment.
     */
    public final ElfProgramType type;
    /**
     * Segment-dependent flags (position for 32-bit structure). See above p_flags field for flag definitions.
     */
    public final ProgramFlags flags;
    /**
     * Offset of the segment in the file image.
     */
    public final Word offset;
    /**
     * Virtual address of the segment in memory.
     */
    public final Word virtual_address;
    /**
     * On systems where physical address is relevant, reserved for segment's physical address.
     */
    public final Word physical_address;
    /**
     * Size in bytes of the segment in the file image. May be 0.
     */
    public final Word file_size;
    /**
     * Size in bytes of the segment in memory. May be 0.
     */
    public final Word memory_size;
    /**
     * 0 and 1 specify no alignment. Otherwise should be a positive, integral power of 2, with p_vaddr equating p_offset modulus p_align.
     */
    public final Word align;


    public ElfProgram(Elf<T> elf) throws ElfException {
        this.parent = elf;
        switch(elf.elf_class){
            case S32 -> {
                this.type = ElfProgramType.from_repr(elf.src.getInt());
                this.offset = elf.getWord();
                this.virtual_address = elf.getWord();
                this.physical_address = elf.getWord();
                this.file_size = elf.getWord();
                this.memory_size = elf.getWord();
                this.flags = ProgramFlags.from_repr(elf.src.getInt());
                this.align = elf.getWord();
            }
            case S64 -> {
                this.type = ElfProgramType.from_repr(elf.src.getInt());
                this.flags = ProgramFlags.from_repr(elf.src.getInt());
                this.offset = elf.getWord();
                this.virtual_address = elf.getWord();
                this.physical_address = elf.getWord();
                this.file_size = elf.getWord();
                this.memory_size = elf.getWord();
                this.align = elf.getWord();
            }
            default -> throw new ElfException();
        }
    }

    public List<ElfSection<T>> sections(){
        var start = this.offset.long_value();
        var end = start+this.file_size.long_value();
        return parent.sections.stream().filter(s -> {
            var soff = s.offset.long_value();
            return start <= soff && soff < end;
        }).toList();
    }

    @Override
    public String toString() {
        return "\tElfProgram{" +
                "\n\t\ttype=" + type +
                ", \n\t\tflags=" + flags +
                ", \n\t\toffset=" + offset.to_str_hex() +
                ", \n\t\tvirtual_address=" + virtual_address.to_str_hex() +
                ", \n\t\tphysical_address=" + physical_address.to_str_hex() +
                ", \n\t\tfile_size=" + file_size.to_str_hex() +
                ", \n\t\tmemory_size=" + memory_size.to_str_hex() +
                ", \n\t\talign=" + align.to_str_hex() +
                ", \n\t\tsections= [" + String.join(", ", sections().stream().map(s -> parent.str_table().get_str(s.name)).toList()) + "]" +
                "\n\t}";
    }
}
