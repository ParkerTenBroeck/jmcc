package elf.sections;

import elf.Elf;
import elf.enums.ElfSectionType;
import elf.exceptions.ElfException;
import elf.flags.SectionFlags;
import elf.word.Word;

public class ElfSection<T extends Word> {
    public final Elf<T> parent;

    /**
     * An offset to a string in the .shstrtab section that represents the name of this section
     */
    public final int name;
    /**
     * Identifies the type of this header.
     */
    public final ElfSectionType type;
    /**
     * Identifies the attributes of the section.
     */
    public final SectionFlags flags;
    /**
     * Virtual address of the section in memory, for sections that are loaded.
     */
    public final T addr;
    /**
     * Offset of the section in the file image.
     */
    public final T offset;
    /**
     * Size in bytes of the section. May be 0.
     */
    public final T size;
    /**
     * Contains the section index of an associated section. This field is used for several purposes, depending on the type of section.
     */
    public final int link;
    /**
     * Contains extra information about the section. This field is used for several purposes, depending on the type of section.
     */
    public final int info;
    /**
     * Contains the required alignment of the section. This field must be a power of two.
     */
    public final T addr_align;
    /**
     * Contains the size, in bytes, of each entry, for sections that contain fixed-size entries. Otherwise, this field contains zero.
     */
    public final T entry_size;

    public ElfSection(Elf<T> elf) throws ElfException {
        this.parent = elf;
        this.name = elf.src.getInt();
        this.type = ElfSectionType.from_repr(elf.src.getInt());
        this.flags = SectionFlags.from_repr(elf.src.getInt());
        this.addr = elf.getWord();
        this.offset = elf.getWord();
        this.size = elf.getWord();
        this.link = elf.src.getInt();
        this.info = elf.src.getInt();
        this.addr_align = elf.getWord();
        this.entry_size = elf.getWord();
    }

    public byte[] raw_section(){
        var data = new byte[(int)this.size.long_value()];
        this.parent.src.position((int)this.offset.long_value()).get(data);
        return data;
    }

    @Override
    public String toString() {
        return "\tElfSection{" +
                "\n\t\tname=" + "'" + parent.str_table().get_str(name) + "'" + " (" + name + ")" +
                ", \n\t\ttype=" + type +
                ", \n\t\tflags=" + flags +
                ", \n\t\taddr=" + addr.to_str_hex() +
                ", \n\t\toffset=" + offset.to_str_hex() +
                ", \n\t\tsize=" + size.to_str_hex() +
                ", \n\t\tlink=" + link +
                ", \n\t\tinfo=" + info +
                ", \n\t\taddr_align=" + addr_align.to_str_hex() +
                ", \n\t\tentry_size=" + entry_size.to_str_hex() +
                "\n\t}";
    }
}
