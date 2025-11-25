package elf.flags;

import java.util.ArrayList;
import java.util.List;

public class SectionFlags {
    public final List<SectionFlag> flags;

    private SectionFlags(List<SectionFlag> flags) {
        this.flags = flags;
    }

    public enum SectionFlag {
        /**
         * Writable
         */
        WRITE(0x1),

        /**
         * Occupies memory during execution
         */
        ALLOC(0x2),

        /**
         * Executable
         */
        EXECINSTR(0x4),

        /**
         * Might be merged
         */
        MERGE(0x10),

        /**
         * Contains null-terminated strings
         */
        STRINGS(0x20),

        /**
         * 'sh_info' contains SHT index
         */
        INFO_LINK(0x40),

        /**
         * Preserve order after combining
         */
        LINK_ORDER(0x80),

        /**
         * Non-standard OS specific handling required
         */
        OS_NONCONFORMING(0x100),

        /**
         * Section is member of a group
         */
        GROUP(0x200),

        /**
         * Section hold thread-local data
         */
        TLS(0x400),

        /**
         * OS-specific
         */
        MASKOS(0x0FF00000),

        /**
         * Processor-specific
         */
        MASKPROC(0xF0000000),

        /**
         * Special ordering requirement (Solaris)
         */
        ORDERED(0x4000000),

        /**
         * Section is excluded unless referenced or allocated (Solaris)
         */
        EXCLUDE(0x8000000);


        public int repr() {
            return this.repr;
        }

        public final int repr;

        SectionFlag(int repr) {
            this.repr = repr;
        }
        }

    public static SectionFlags from_repr(int repr) {
        var list = new ArrayList<SectionFlag>();
        for (var flag : SectionFlag.values()) {
            if ((flag.repr & repr) != 0) list.add(flag);
        }
        return new SectionFlags(List.copyOf(list));
    }

    public int repr() {
        int repr = 0;
        for (var flag : flags) {
            repr |= flag.repr();
        }
        return repr;
    }

    @Override
    public String toString() {
        return String.format("0x%08X", repr()) + (!this.flags.isEmpty() ?" ":"") + String.join(", ", this.flags.stream().map(Object::toString).toList());
    }
}
