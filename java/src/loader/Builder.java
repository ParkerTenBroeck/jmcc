package loader;

import runtime.CPU;
import runtime.Memory;
import runtime.MipsProgram;
import runtime.Register;

import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.SwitchCase;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.util.HashMap;
import java.util.List;

import static loader.MipsDecoder.decode;

public class Builder {

    private static final ClassDesc CD_CPU = ClassDesc.ofDescriptor(CPU.class.descriptorString());
    private static final MethodTypeDesc MD_void_CPU = MethodTypeDesc.of(ConstantDescs.CD_void, CD_CPU);

    private static final ClassDesc CD_Memory = ClassDesc.ofDescriptor(Memory.class.descriptorString());

    private static final int CPU_SLOT = 1;
    private static final int MEMORY_SLOT = 2;
    private static final int PC_REGISTER_SLOT = 3;
    private static final int REGISTER_BASE_SLOT = 4;

    private static final int BRANCH_RETURN_PC_OFFSET = 8;


    public static byte[] build_class_file(String className, MipsLoader.RawMipsProgram program){
        var CD_this = ClassDesc.of(className);
        return ClassFile.of().build(CD_this, cb -> {
            cb.withFlags(ClassFile.ACC_PUBLIC);

            var iface = ClassDesc.ofDescriptor(MipsProgram.class.descriptorString());
            cb.withInterfaces(cb.constantPool().classEntry(iface));

            cb.withMethod(ConstantDescs.INIT_NAME, MethodTypeDesc.of(ConstantDescs.CD_void), ClassFile.ACC_PUBLIC, mb -> {
                mb.withCode(
                        cob -> cob.aload(0)
                                .invokespecial(ConstantDescs.CD_Object,
                                        ConstantDescs.INIT_NAME, ConstantDescs.MTD_void)
                                .return_());
            });

            cb.withMethod("run", MD_void_CPU, ClassFile.ACC_PUBLIC, mb -> {
                mb.withCode(cob -> {
                    new MipsMethodBuilder(CD_this, program, cob).build_method();
                });
            });
        });
    }

    private static class MipsMethodBuilder{
        private final CodeBuilder cob;
        private final ClassDesc CD_this;
        private final Label switchLabel;
        private final List<MipsDecoder.InstructionAndAddress> instructions;
        private final HashMap<Integer, Label> labelAddressMap = new HashMap<>();

        private MipsMethodBuilder(ClassDesc cdThis, MipsLoader.RawMipsProgram program, CodeBuilder cob) {
            this.cob = cob;
            this.CD_this = cdThis;
            this.switchLabel = cob.newLabel();
            this.instructions = decode(program);
            for(var instr : this.instructions){
                if(!this.labelAddressMap.containsKey(instr.hashCode()))
                    this.labelAddressMap.put(instr.address(), cob.newLabel());
            }
        }

        private MipsMethodBuilder load_register(Register reg){
            if (reg == Register.Zero){
                cob.iconst_0();
            }else{
                cob.iload(reg.repr+REGISTER_BASE_SLOT-1);
            }
            return this;
        }

        private MipsMethodBuilder store_register(Register reg){
            if (reg == Register.Zero) {
                cob.pop();
            }else{
                cob.istore(reg.repr+REGISTER_BASE_SLOT-1);
            }
            return this;
        }

        private MipsMethodBuilder load_value(MipsDecoder.Value value){
            switch(value){
                case MipsDecoder.F32Const(var f) -> cob.loadConstant(f);
                case MipsDecoder.I32Const(var i) -> cob.loadConstant(i);
                case MipsDecoder.Reg(var reg) -> load_register(reg);
            }
            return this;
        }

        private MipsMethodBuilder load_pc_from_cpu(){
            cob.aload(CPU_SLOT).getfield(CD_CPU, "pc", ConstantDescs.CD_int).istore(PC_REGISTER_SLOT);
            return this;
        }

        private MipsMethodBuilder load_memory_from_cpu(){
            cob.aload(CPU_SLOT).getfield(CD_CPU, "memory", CD_Memory).astore(MEMORY_SLOT);
            return this;
        }

        private MipsMethodBuilder load_registers_from_cpu(){
            for(int i = 1; i < Register.values().length; i ++){
                cob.aload(CPU_SLOT)
                        .getfield(CD_CPU, "registers", ConstantDescs.CD_int.arrayType())
                        .loadConstant(i)
                        .iaload()
                        .istore(REGISTER_BASE_SLOT+i-1);
            }
            return this;
        }


        private MipsMethodBuilder store_pc_from_cpu(){
            cob.aload(CPU_SLOT).iload(PC_REGISTER_SLOT).putfield(CD_CPU, "pc", ConstantDescs.CD_int);
            return this;
        }

        private MipsMethodBuilder store_registers_from_cpu(){
            for(int i = 1; i < Register.values().length; i ++){
                cob.aload(CPU_SLOT)
                        .getfield(CD_CPU, "registers", ConstantDescs.CD_int.arrayType())
                        .loadConstant(i)
                        .iload(REGISTER_BASE_SLOT+i-1)
                        .iastore();
            }
            return this;
        }

        private MipsMethodBuilder instruction(MipsDecoder.Instruction instruction, int pc_addr){
            System.out.println(instruction);
            switch(instruction){
                case MipsDecoder.Arith(var type, var opt, var dest, var lhs, var rhs) -> {
                    boolean use_long = opt == MipsDecoder.ArithOperation.Mult
                            ||opt == MipsDecoder.ArithOperation.Multu
                            ||opt == MipsDecoder.ArithOperation.Divu
                            ||opt == MipsDecoder.ArithOperation.Remu
                            || opt == MipsDecoder.ArithOperation.Sltu;
                    boolean unsigned_long = opt == MipsDecoder.ArithOperation.Multu
                            ||opt == MipsDecoder.ArithOperation.Divu
                            ||opt == MipsDecoder.ArithOperation.Remu
                            || opt == MipsDecoder.ArithOperation.Sltu;

                    load_value(lhs);
                    if(use_long) cob.i2l();
                    if(unsigned_long) cob.loadConstant(0xFFFFFFFFL).land();

                    load_value(rhs);
                    if(use_long) cob.i2l();
                    if(unsigned_long) cob.loadConstant(0xFFFFFFFFL).land();

                    if(type != MipsDecoder.ArithType.I32) throw new RuntimeException();
                    switch(opt){
                        case Add, Addu -> cob.iadd();
                        case Sub, Subu -> cob.isub();
                        case Mult, Multu -> {
                            cob
                                .lmul()
                                .dup2()
                                .loadConstant(32).lushr().l2i();
                            store_register(Register.from_repr(dest.register().repr+1));
                            cob.loadConstant(0xFFFFFFFFL).land().l2i();
                        }
                        case Divu -> cob.ldiv().l2i();
                        case Div -> cob.idiv();
                        case Remu -> cob.lrem().l2i();
                        case Rem -> cob.irem();
                        case And -> cob.iand();
                        case Xor -> cob.ixor();
                        case Nor -> cob.ior().iconst_m1().ixor();
                        case Or -> cob.ior();
                        case Sll -> cob.ishl();
                        case Srl -> cob.ishr();
                        case Sra -> cob.iushr();
                        case Slt -> cob.ifThenElse(Opcode.IF_ICMPLT,
                                CodeBuilder::iconst_1, CodeBuilder::iconst_0
                        );
                        case Sltu -> cob.lcmp().ifThenElse(Opcode.IFLT,
                                CodeBuilder::iconst_1, CodeBuilder::iconst_0
                        );
                    }

                    store_register(dest.register());
                }
                case MipsDecoder.Branch(var kind, var lhs, var rhs, var target) -> {
                    load_value(lhs).load_value(rhs);
                    switch (kind) {
                        case Eq -> cob.if_icmpeq(labelAddressMap.get(target));
                        case Neq -> cob.if_icmpne(labelAddressMap.get(target));
                        case Bge -> cob.if_icmpge(labelAddressMap.get(target));
                        case Bgt -> cob.if_icmpgt(labelAddressMap.get(target));
                        case Blt -> cob.if_icmplt(labelAddressMap.get(target));
                        case Ble -> cob.if_icmple(labelAddressMap.get(target));
                    }
                }
                case MipsDecoder.BranchAndLink(var kind, var lhs, var rhs, var target) -> {
                    load_value(lhs).load_value(rhs);

                    load_register(Register.RA);
                    cob.istore(PC_REGISTER_SLOT);

                    cob.loadConstant(pc_addr+BRANCH_RETURN_PC_OFFSET);
                    store_register(Register.RA);
                    switch (kind) {
                        case Eq -> cob.if_icmpeq(labelAddressMap.get(target));
                        case Neq -> cob.if_icmpne(labelAddressMap.get(target));
                        case Bge -> cob.if_icmpge(labelAddressMap.get(target));
                        case Bgt -> cob.if_icmpgt(labelAddressMap.get(target));
                        case Blt -> cob.if_icmplt(labelAddressMap.get(target));
                        case Ble -> cob.if_icmple(labelAddressMap.get(target));
                    }

                    cob.iload(PC_REGISTER_SLOT);
                    store_register(Register.RA);
                }
                case MipsDecoder.Jump(MipsDecoder.I32Const(var value)) -> {
                    cob.goto_(labelAddressMap.get(value));
                }
                case MipsDecoder.Jump(MipsDecoder.Reg(var reg)) -> {
                    load_register(reg);
                    cob.goto_(switchLabel);
                }
                case MipsDecoder.JumpAndLink(MipsDecoder.I32Const(var value)) -> {
                    cob.loadConstant(pc_addr+BRANCH_RETURN_PC_OFFSET);
                    store_register(Register.RA);
                    cob.goto_(labelAddressMap.get(value));
                }
                case MipsDecoder.JumpAndLink(MipsDecoder.Reg(var reg)) -> {
                    cob.loadConstant(pc_addr+BRANCH_RETURN_PC_OFFSET);
                    store_register(Register.RA);
                    load_register(reg);
                    cob.goto_(switchLabel);
                }
                case MipsDecoder.LoadUpperImmediate(MipsDecoder.Reg(var dest), var value) -> {
                    cob.loadConstant(value).loadConstant(16).ishl();
                    store_register(dest);
                }
                case MipsDecoder.MemoryLoad(var kind, MipsDecoder.Reg(var dest), var offset, MipsDecoder.Reg(var idx)) -> {
                    cob.aload(MEMORY_SLOT);
                    load_register(idx);
                    cob.loadConstant(offset).iadd();
                    switch (kind) {
                        case I8 -> cob.invokevirtual(CD_Memory, "load_i8", MethodTypeDesc.of(ConstantDescs.CD_byte, ConstantDescs.CD_int));
                        case U8 -> cob.invokevirtual(CD_Memory, "load_u8", MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_int));
                        case I16 -> cob.invokevirtual(CD_Memory, "load_i16", MethodTypeDesc.of(ConstantDescs.CD_short, ConstantDescs.CD_int));
                        case U16 -> cob.invokevirtual(CD_Memory, "load_u16", MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_int));
                        case I32, U32 -> cob.invokevirtual(CD_Memory, "load_i32", MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_int));
                        case I64, U64 -> cob.invokevirtual(CD_Memory, "load_i64", MethodTypeDesc.of(ConstantDescs.CD_long, ConstantDescs.CD_int));
                    }
                    store_register(dest);
                }
                case MipsDecoder.MemoryStore(var kind, MipsDecoder.Reg(var src), var offset, MipsDecoder.Reg(var idx)) -> {
                    cob.aload(MEMORY_SLOT);
                    load_register(idx);
                    cob.loadConstant(offset).iadd();
                    load_register(src);
                    switch (kind) {
                        case I8, U8 -> cob.invokevirtual(CD_Memory, "store_i8", MethodTypeDesc.of(ConstantDescs.CD_void, ConstantDescs.CD_int, ConstantDescs.CD_byte));
                        case I16, U16 -> cob.invokevirtual(CD_Memory, "store_i16", MethodTypeDesc.of(ConstantDescs.CD_void, ConstantDescs.CD_int, ConstantDescs.CD_short));
                        case I32, U32 -> cob.invokevirtual(CD_Memory, "store_i32", MethodTypeDesc.of(ConstantDescs.CD_void, ConstantDescs.CD_int, ConstantDescs.CD_int));
                        case I64, U64 -> cob.invokevirtual(CD_Memory, "store_i64", MethodTypeDesc.of(ConstantDescs.CD_void, ConstantDescs.CD_int, ConstantDescs.CD_long));
                    }
                }
                case MipsDecoder.Break _ -> {
                    store_pc_from_cpu().store_registers_from_cpu();
                    cob.aload(CPU_SLOT).invokevirtual(CD_CPU, "breakpoint", MethodTypeDesc.of(ConstantDescs.CD_void));
                    load_memory_from_cpu().load_pc_from_cpu().load_registers_from_cpu();
                }
                case MipsDecoder.Syscall _ -> {
                    store_pc_from_cpu().store_registers_from_cpu();
                    cob.aload(CPU_SLOT).invokevirtual(CD_CPU, "syscall", MethodTypeDesc.of(ConstantDescs.CD_void));
                    load_memory_from_cpu().load_pc_from_cpu().load_registers_from_cpu();
                }
                case MipsDecoder.CompositeInstruction(var list) -> {
                    for(var instr : list){
                        instruction(instr, pc_addr);
                    }
                }
                case MipsDecoder.Unknown unknown -> {
                    System.out.println(unknown);
//                    throw new RuntimeException(unknown.toString());
                }
            }
            return this;
        }


        MipsMethodBuilder build_method(){
            cob.localVariable(0, "this", CD_this, cob.startLabel(), cob.endLabel());
            cob.localVariable(CPU_SLOT, "cpu", CD_CPU, cob.startLabel(), cob.endLabel());
            cob.localVariable(MEMORY_SLOT, "memory", CD_Memory, cob.startLabel(), cob.endLabel());
            cob.localVariable(PC_REGISTER_SLOT, "pc", ConstantDescs.CD_int, cob.startLabel(), cob.endLabel());
            for(int i = 1; i < Register.values().length; i ++){
                cob.localVariable(REGISTER_BASE_SLOT+i-1, Register.values()[i].name(), ConstantDescs.CD_int, cob.startLabel(), cob.endLabel());
            }
            load_memory_from_cpu().load_pc_from_cpu().load_registers_from_cpu();

            var error = cob.newLabel();

            cob.iload(PC_REGISTER_SLOT);
            cob.labelBinding(switchLabel).iconst_2().iushr();
            cob.tableswitch(error, labelAddressMap.entrySet().stream().map(i -> SwitchCase.of(i.getKey()/4, i.getValue())).toList());

            int current_adderss = -1;
            for(var instr : this.instructions){
                if(current_adderss != instr.address())
                    cob.labelBinding(labelAddressMap.get(instr.address()));
                current_adderss = instr.address();
                instruction(instr.instr(), instr.address());
            }

            var end = cob.newLabel();
            cob.goto_(end);
            {
                cob.labelBinding(error).new_(ConstantDescs.CD_Exception).dup()
//                        .lo
//                        .invokestatic(CD_CPU, "", MethodTypeDesc.of(ConstantDescs.CD_String, ConstantDescs.CD_int))
                        .loadConstant("Invalid Execute Address")
                        .invokespecial(ConstantDescs.CD_Exception, ConstantDescs.INIT_NAME, MethodTypeDesc.of(ConstantDescs.CD_void, ConstantDescs.CD_String)).athrow();
            }
            cob.labelBinding(end);

            store_pc_from_cpu().store_registers_from_cpu();
            cob.return_();
            return this;
        }


    }
}
