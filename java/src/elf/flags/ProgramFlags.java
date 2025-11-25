package elf.flags;

import java.util.ArrayList;
import java.util.List;

public class ProgramFlags {
    public final List<ProgramFlag> flags;

    private ProgramFlags(List<ProgramFlag> flags) {
        this.flags = flags;
    }

    public enum ProgramFlag {
        /**
         * Executable
         */
        X(0x1),
        /**
         * Writeable
         */
        W(0x2),
        /**
         * Readable
         */
        R(0x4);

        public int repr(){
            return this.repr;
        }
        public final int repr;

        ProgramFlag(int repr){
            this.repr = repr;
        }
    }

    public static ProgramFlags from_repr(int repr){
        var list = new ArrayList<ProgramFlag>();
        for(var flag : ProgramFlag.values()){
            if((flag.repr&repr) != 0)list.add(flag);
        }
        return new ProgramFlags(List.copyOf(list));
    }

    public int repr(){
        int repr = 0;
        for(var flag : flags){
            repr |= flag.repr();
        }
        return repr;
    }

    @Override
    public String toString() {
        return String.format("0x%08X", repr()) + (!this.flags.isEmpty() ?" ":"") + String.join(", ", this.flags.stream().map(Object::toString).toList());
    }
}
