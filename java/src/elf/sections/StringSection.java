package elf.sections;

import elf.Elf;
import elf.exceptions.ElfException;
import elf.word.Word;

import java.util.HashMap;

public class StringSection<T extends Word> extends ElfSection<T>{
    private final HashMap<Long, String> map = new HashMap<>();

    public StringSection(Elf<T> elf) throws ElfException {
        super(elf);

        long offset = this.offset.long_value();
        long end = this.size.long_value()+offset;
        var builder = new StringBuilder();

        elf.src.position((int)offset);
        elf.src.limit((int)end);
        while(elf.src.remaining() != 0){
            long str_idx = elf.src.position()-offset;
            byte current = elf.src.get();
            while(current!=0){
                builder.append((char)current);
                current = elf.src.get();
            }
            map.put(str_idx, builder.toString());
            builder.delete(0, builder.length());
        }
        elf.src.limit(elf.src.capacity());
    }

    public String get_str(long offset){
        return this.map.get(offset);
    }

}
