package loader;

import runtime.CPU;
import runtime.MipsProgram;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static loader.Builder.build_class_file;

public class MipsLoader {
    public static Class<?> load_mips_program(RawMipsProgram program){
        var className = String.format("MIPS$%08X", Arrays.hashCode(program.binary.array()));
        var bytes = build_class_file(className, program);

//        try {
//            Files.write(Path.of(className + ".class"), bytes);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        try {
            return new ClassLoader(MipsLoader.class.getClassLoader()){
                @Override
                public Class<?> loadClass(String name) throws ClassNotFoundException {
                    if(name.startsWith("MIPS$")){
                        return defineClass(name, bytes, 0, bytes.length);
                    }else{
                        return super.loadClass(name);
                    }
                }
            }.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public record RawMipsProgram(ByteBuffer binary, int entry_point, int base_addr, int exec_start, int exec_end){

        public static RawMipsProgram load(Path path) throws IOException {
            var file = ByteBuffer.wrap(Files.readAllBytes(path));
            var entry = file.getInt(0);
            var exec_start = file.getInt(4);
            var exec_end = file.getInt(8);
            file.position(12);
            return new RawMipsProgram(file, entry, exec_start, exec_start, exec_end);
        }

        public void init(CPU cpu) {
            cpu.pc = this.entry_point;
            cpu.memory.memcopy(this.binary.array(), this.binary.position(), this.binary.remaining(), this.base_addr);
        }
    }

    public static MipsProgram mips_program(RawMipsProgram program){
        try {
            return (MipsProgram) load_mips_program(program).getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
