import elf.Elf;
import elf.exceptions.ElfException;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException, ElfException {
        System.out.println(Elf.load(Path.of("../mips/a.out")));
//        var raw_program = MipsLoader.RawMipsProgram.load(Path.of("../mips/out.prog"));
//        var program = MipsLoader.mips_program(raw_program);
//
//        var cpu = new CPU();
//        raw_program.init(cpu);
//        program.run(cpu);
//        System.out.println(cpu);
    }
}