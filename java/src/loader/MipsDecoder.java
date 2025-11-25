package loader;

import runtime.Register;

import java.util.ArrayList;
import java.util.List;

public class MipsDecoder {

    public enum ArithType {
        I32,
        I64,
        F32,
        F64,
    }

    public enum ArithOperation{
        Add,
        Addu,
        Sub,
        Subu,
        Mult,
        Multu,
        Div,
        Divu,
        Rem,
        Remu,

        And,
        Xor,
        Nor,
        Or,

        Sll,
        Srl,
        Sra,
        Slt, Sltu,
    }

    public enum MemoryKind {
        I8,
        I16,
        I32,
        I64,
        U8,
        U16,
        U32,
        U64,
    }

    public enum BranchKind{
        Eq, Neq, Ble, Bgt, Bge, Blt,
    }

    public sealed interface WordOrReg{};

    public sealed interface Value{}
    public record Reg(Register register) implements Value, WordOrReg{}
    public record I32Const(int value) implements Value, WordOrReg{}
    public record F32Const(float value) implements Value{}

    public sealed interface Instruction{}
    public sealed interface BranchingInstruction{}
    public record Arith(ArithType type, ArithOperation opt, Reg dest, Reg lhs, Value rhs) implements Instruction{}
    public record MemoryStore(MemoryKind size, Reg src, int offset, Reg idx) implements Instruction{}
    public record MemoryLoad(MemoryKind size, Reg dest, int offset, Reg idx) implements Instruction{}
    public record Jump(WordOrReg dest) implements Instruction, BranchingInstruction {}
    public record JumpAndLink(WordOrReg dest) implements Instruction, BranchingInstruction {}
    public record Branch(BranchKind kind, Value lhs, Value rhs, int addr_dest) implements Instruction, BranchingInstruction{}
    public record BranchAndLink(BranchKind kind, Value lhs, Value rhs, int addr_dest) implements Instruction, BranchingInstruction{}
    public record LoadUpperImmediate(Reg dest, int value) implements Instruction{}
    public record CompositeInstruction(List<Instruction> instructions) implements Instruction{};


    public record Break() implements Instruction{}
    public record Syscall() implements Instruction{}

    public record Unknown(int ins) implements Instruction{}

    public record InstructionAndAddress(Instruction instr, int address){}


    public static Instruction decode(int instr, int pc) {
        int opcode = (instr >>> 26) & 0x3F;

        int rs = (instr >>> 21) & 0x1F;
        int rt = (instr >>> 16) & 0x1F;
        int rd = (instr >>> 11) & 0x1F;
        // r type
        int shamt = (instr >>> 6) & 0x1F;
        int funct = instr & 0x3F;


        // immediate
        int imm = instr & 0xFFFF;
        // signed immediate
        int simm = (short) (instr & 0xFFFF);

        // jump
        int target = instr & 0x03FFFFFF;
        int addr_j = (pc & 0xF0000000) | (target << 2);

        // branch
        int addr_b = pc + 4 + (simm << 2);


        return switch (opcode) {

            // ---------------------------------------------------------
            //                    R–TYPE (opcode = 0)
            // ---------------------------------------------------------
            case 0 -> switch (funct) {
                case 0x00 -> new Arith(ArithType.I32, ArithOperation.Sll, r(rd), r(rt), new I32Const(shamt));
                case 0x02 -> new Arith(ArithType.I32, ArithOperation.Srl, r(rd), r(rt), new I32Const(shamt));
                case 0x03 -> new Arith(ArithType.I32, ArithOperation.Sra, r(rd), r(rt), new I32Const(shamt));
                case 0x04 -> new Arith(ArithType.I32, ArithOperation.Sll, r(rd), r(rt), r(rs));
                case 0x06 -> new Arith(ArithType.I32, ArithOperation.Srl, r(rd), r(rt), r(rs));
                case 0x07 -> new Arith(ArithType.I32, ArithOperation.Sra, r(rd), r(rt), r(rs));

                case 0x08 -> new Jump(r(rs));
                case 0x09 -> new JumpAndLink(r(rs));

                case 0x0D -> new Break();
                case 0x0C -> new Syscall();

                case 0x10 -> new Arith(ArithType.I32, ArithOperation.Add, r(rd), new Reg(Register.Hi), new I32Const(0));
                case 0x11 -> new Arith(ArithType.I32, ArithOperation.Add, new Reg(Register.Hi), r(rd), new I32Const(0));
                case 0x12 -> new Arith(ArithType.I32, ArithOperation.Add, r(rd), new Reg(Register.Lo), new I32Const(0));
                case 0x13 -> new Arith(ArithType.I32, ArithOperation.Add, new Reg(Register.Lo), r(rd), new I32Const(0));

                case 0x18 -> new Arith(ArithType.I32, ArithOperation.Mult, new Reg(Register.Lo), r(rs), r(rt));
                case 0x19 -> new Arith(ArithType.I32, ArithOperation.Multu, new Reg(Register.Lo), r(rs), r(rt));

                case 0x1A -> new CompositeInstruction(List.of(
                    new Arith(ArithType.I32, ArithOperation.Div, new Reg(Register.Lo), r(rs), r(rt)),
                    new Arith(ArithType.I32, ArithOperation.Rem, new Reg(Register.Hi), r(rs), r(rt))
                ));
                case 0x1B -> new CompositeInstruction(List.of(
                    new Arith(ArithType.I32, ArithOperation.Divu, new Reg(Register.Lo), r(rs), r(rt)),
                    new Arith(ArithType.I32, ArithOperation.Remu, new Reg(Register.Hi), r(rs), r(rt))
                ));

                case 0x20 -> new Arith(ArithType.I32, ArithOperation.Add, r(rd), r(rs), r(rt));
                case 0x21 -> new Arith(ArithType.I32, ArithOperation.Addu, r(rd), r(rs), r(rt));
                case 0x22 -> new Arith(ArithType.I32, ArithOperation.Sub, r(rd), r(rs), r(rt));
                case 0x23 -> new Arith(ArithType.I32, ArithOperation.Subu, r(rd), r(rs), r(rt));
                case 0x24 -> new Arith(ArithType.I32, ArithOperation.And, r(rd), r(rs), r(rt));
                case 0x25 -> new Arith(ArithType.I32, ArithOperation.Or, r(rd), r(rs), r(rt));
                case 0x26 -> new Arith(ArithType.I32, ArithOperation.Xor, r(rd), r(rs), r(rt));
                case 0x27 -> new Arith(ArithType.I32, ArithOperation.Nor, r(rd), r(rs), r(rt));
                case 0x2A -> new Arith(ArithType.I32, ArithOperation.Slt, r(rd), r(rs), r(rt));
                case 0x2B -> new Arith(ArithType.I32, ArithOperation.Sltu, r(rd), r(rs), r(rt));

                default -> new Unknown(instr);
            };

            // ---------------------------------------------------------
            //                    J–TYPE
            // ---------------------------------------------------------
            case 0x02 -> new Jump(new I32Const(addr_j));
            case 0x03 -> new JumpAndLink(new I32Const(addr_j));

            // ---------------------------------------------------------
            //                    I–TYPE
            // ---------------------------------------------------------
            case 0x04 -> new Branch(BranchKind.Eq, r(rs), r(rt), addr_b);
            case 0x05 -> new Branch(BranchKind.Neq, r(rs), r(rt), addr_b);
            case 0x06 -> new Branch(BranchKind.Ble, r(rs), new I32Const(0), addr_b);
            case 0x07 -> new Branch(BranchKind.Bgt, r(rs), new I32Const(0), addr_b);

            case 0x01 -> switch (rt){
                case 0x00 -> new Branch(BranchKind.Blt, r(rs), new I32Const(0), addr_b);
                case 0x01 -> new Branch(BranchKind.Bge, r(rs), new I32Const(0), addr_b);
                case 0x10 -> new BranchAndLink(BranchKind.Blt, r(rs), new I32Const(0), addr_b);
                case 0x11 -> new BranchAndLink(BranchKind.Bge, r(rs), new I32Const(0), addr_b);
                default -> new Unknown(instr);
            };


            case 0x08 -> new Arith(ArithType.I32, ArithOperation.Add, r(rt), r(rs), new I32Const(simm));
            case 0x09 -> new Arith(ArithType.I32, ArithOperation.Addu, r(rt), r(rs), new I32Const(simm));
            case 0x0C -> new Arith(ArithType.I32, ArithOperation.And, r(rt), r(rs), new I32Const(imm));
            case 0x0D -> new Arith(ArithType.I32, ArithOperation.Or, r(rt), r(rs), new I32Const(imm));
            case 0x0E -> new Arith(ArithType.I32, ArithOperation.Xor, r(rt), r(rs), new I32Const(imm));
            case 0x0A -> new Arith(ArithType.I32, ArithOperation.Slt, r(rt), r(rs), new I32Const(simm));
            case 0x0B -> new Arith(ArithType.I32, ArithOperation.Sltu, r(rt), r(rs), new I32Const(simm));

            case 0x0F -> new LoadUpperImmediate(r(rt), imm);

            case 0x23 -> new MemoryLoad(MemoryKind.I32, r(rt), simm, r(rs));
            case 0x21 -> new MemoryLoad(MemoryKind.I16, r(rt), simm, r(rs));
            case 0x25 -> new MemoryLoad(MemoryKind.U16, r(rt), simm, r(rs));
            case 0x20 -> new MemoryLoad(MemoryKind.I8, r(rt), simm, r(rs));
            case 0x24 -> new MemoryLoad(MemoryKind.U8, r(rt), simm, r(rs));

            case 0x2B -> new MemoryStore(MemoryKind.I32, r(rt), simm, r(rs));
            case 0x29 -> new MemoryStore(MemoryKind.I16, r(rt), simm, r(rs));
            case 0x28 -> new MemoryStore(MemoryKind.I8, r(rt), simm, r(rs));

            default -> new Unknown(instr);
        };
    }

    public static Reg r(int index) {
        return new Reg(Register.from_repr(index));
    }

    public static List<InstructionAndAddress> decode(MipsLoader.RawMipsProgram program){
        var list = new ArrayList<InstructionAndAddress>();
        for(int i = program.exec_start(); i < program.exec_end(); i += 4){

            var instr = program.binary().getInt(program.binary().position()+i-program.entry_point());
            var decoded = decode(instr, i);
            if (decoded instanceof BranchingInstruction) {
                var bds_instr = program.binary().getInt(program.binary().position() + i + 4 - program.entry_point());
                list.add(new InstructionAndAddress(decode(bds_instr, i + 4), i));
                list.add(new InstructionAndAddress(decoded, i));
                i += 4;
            } else {
                list.add(new InstructionAndAddress(decoded, i));
            }
        }
        list.add(new InstructionAndAddress(new Arith(ArithType.I32, ArithOperation.Sll, new Reg(Register.Zero), new Reg(Register.Zero), new I32Const(0)), program.exec_end()));
        return list;
    }

}
