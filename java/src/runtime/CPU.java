package runtime;

import java.util.Arrays;

public class CPU {
    public Memory memory = new Memory();
    public int pc;
    public int[] registers = new int[Register.values().length];


    public void syscall(){
        switch(registers[Register.V0.repr]){
            case 0 -> throw new RuntimeException("Exit Syscall");
            case 1 -> {
                int ptr = registers[Register.A0.repr];
                int size = registers[Register.A1.repr];
                for(int i = 0; i < size; i ++){
                    System.out.print((char)memory.load_u8(ptr+i));
                }
            }
            case 2 -> {
                System.out.print(registers[Register.A0.repr]);
            }
            default -> throw new RuntimeException(String.format("Unknown Syscall %d", registers[Register.V0.repr]));
        }
    }

    public void breakpoint(){
        System.out.println("break point");
    }

    @Override
    public String toString() {
        return "CPU{" +
                "memory=" + memory +
                ", pc=" + pc +
                ", registers=" + Arrays.toString(registers) +
                '}';
    }
}
