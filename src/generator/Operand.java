import java.util.*;

public abstract class Operand
{
    String type;

    public Operand(String type)
    {
        this.type = type;
    }

    public String toString()
    {
        return type;
    }

    public abstract int getSize();

    public abstract String define(int arg);

    public abstract String construct(int arg);

    public abstract String load(int arg);

    public abstract String set(int arg);

    public abstract String get(int arg);

    public static class Reg extends Operand
    {
        final int size;

        public Reg(String name, int size)
        {
            super(name);
            this.size = size;
        }

        public int getSize()
        {
            return size;
        }
        
        public String define(int arg)
        {
            return "    final int "+getVal(arg)+";\n";
        }

        public String construct(int arg)
        {
            return "        "+getVal(arg) + " = Processor.getRegIndex(parent.operand["+(arg-1)+"].toString());";
        }

        public String load(int arg)
        {
            return "        Reg op"+arg+" = cpu.regs["+getVal(arg)+"];";
        }

        public String set(int arg)
        {
            return "op"+arg+".set"+getSize()+"(";
        }

        public String get(int arg)
        {
            return "op"+arg+".get"+getSize()+"()";
        }

        private String getVal(int arg)
        {
            return "op"+arg+"Index";
        }
    }

    public static class SpecificReg extends Operand
    {
        final int size;
        final String name;

        public SpecificReg(String type, String name, int size)
        {
            super(type);
            this.size = size;
            this.name = name;
        }

        public int getSize()
        {
            return size;
        }
        
        public String define(int arg)
        {
            return "";
        }

        public String construct(int arg)
        {
            return "";
        }

        public String load(int arg)
        {
            return "";
        }

        public String set(int arg)
        {
            return name+".set"+getSize()+"(";
        }

        public String get(int arg)
        {
            return name+".get"+getSize()+"()";
        }
    }

    public static class Mem extends Operand
    {
        final int size;

        public Mem(String name, int size)
        {
            super(name);
            this.size = size;
        }

        public int getSize()
        {
            return size;
        }
        
        public String define(int arg)
        {
            return "    final Address op"+arg+";\n";
        }

        public String construct(int arg)
        {
            return "        op"+arg+" = new Address(parent.operand["+(arg-1)+"]);";
        }

        public String load(int arg)
        {
            return "";
        }

        public String set(int arg)
        {
            return "op"+arg+".set"+getSize()+"(cpu, ";
        }

        public String get(int arg)
        {
            return "op"+arg+".get"+getSize()+"(cpu)";
        }
    }

    public static class Segment extends Operand
    {
        final int size=16;

        public Segment(String name)
        {
            super(name);
        }

        public int getSize()
        {
            return size;
        }
        
        public String define(int arg)
        {
            return "    final int segIndex;\n";
        }

        public String construct(int arg)
        {
            return "        segIndex = Processor.getSegmentIndex(parent.operand["+(arg-1)+"].toString());";
        }

        public String load(int arg)
        {
            if (arg != 1)
                return "        Segment seg = cpu.segs[segIndex];";
            else 
                return "";
        }

        public String set(int arg)
        {
            return "cpu.setSeg(segIndex, ";
        }

        public String get(int arg)
        {
            return "seg.getSelector()";
        }
    }

    public static class SpecificSegment extends Operand
    {
        final int size=16;
        final String name;

        public SpecificSegment(String type, String name)
        {
            super(type);
            this.name = name;
        }

        public int getSize()
        {
            return size;
        }
        
        public String define(int arg)
        {
            return "";
        }

        public String construct(int arg)
        {
            return "";
        }

        public String load(int arg)
        {
            return "";
        }

        public String set(int arg)
        {
            return "cpu."+name+"(";
        }

        public String get(int arg)
        {
            return "cpu."+name+"()";
        }
    }

    public static class Address extends Operand
    {
        public Address(String name)
        {
            super(name);
        }

        public int getSize()
        {
            return 0;
        }
        
        public String define(int arg)
        {
            return "    final Address op"+arg+";\n";
        }

        public String construct(int arg)
        {
            return "        op"+arg+" = new Address(parent.operand["+(arg-1)+"]);";
        }

        public String load(int arg)
        {
            return "";
        }

        public String set(int arg)
        {
            return "";
        }

        public String get(int arg)
        {
            return "op"+arg+".get(cpu)";
        }
    }

    public static class Immediate extends Operand
    {
        final int size;

        public Immediate(String name, int size)
        {
            super(name);
            this.size = size;
        }

        public int getSize()
        {
            return size;
        }
        
        public String define(int arg)
        {
            return "    final int imm;\n";
        }

        public String construct(int arg)
        {
            return "        imm = ("+cast()+")parent.operand["+(arg-1)+"].lval;";
        }

        private String cast()
        {
            if (size == 8)
                return "byte";
            if (size == 16)
                return "short";
            if (size == 32)
                return "int";
            throw new IllegalStateException("Unknown immediate size "+size);
        }

        public String load(int arg)
        {
            return "";
        }

        public String set(int arg)
        {
            return "";
        }

        public String get(int arg)
        {
            return "imm";
        }
    }

    public static class Jump extends Operand
    {
        final int size;

        public Jump(String name, int size)
        {
            super(name);
            this.size = size;
        }

        public int getSize()
        {
            return size;
        }
        
        public String define(int arg)
        {
            return "    final int jmp, blockLength;\n";
        }

        public String construct(int arg)
        {
            return "        jmp = ("+cast()+")parent.operand["+(arg-1)+"].lval;\n        blockLength = parent.x86Length+(int)parent.eip-blockStart;";
        }

        private String cast()
        {
            if (size == 8)
                return "byte";
            if (size == 16)
                return "short";
            if (size == 32)
                return "int";
            throw new IllegalStateException("Unknown immediate size "+size);
        }

        public String load(int arg)
        {
            return "";
        }

        public String set(int arg)
        {
            return "";
        }

        public String get(int arg)
        {
            return "imm";
        }
    }

    public static class FarPointer extends Operand
    {
        public FarPointer(String name)
        {
            super(name);
        }

        public int getSize()
        {
            return 0;
        }
        
        public String define(int arg)
        {
            return "    final int cs, targetEip;\n";
        }

        public String construct(int arg)
        {
            return "        targetEip = parent.operand["+(arg-1)+"].ptr.off;\n        cs = parent.operand["+(arg-1)+"].ptr.seg;";
        }

        public String load(int arg)
        {
            return "";
        }

        public String set(int arg)
        {
            return "op"+arg+".set"+getSize()+"(cpu, ";
        }

        public String get(int arg)
        {
            return "op"+arg+".get"+getSize()+"(cpu)";
        }
    }

    public static Map<String, String> segs = new HashMap();
    public static Map<String, String> reg8 = new HashMap();
    public static Map<String, String> reg16 = new HashMap();
    static {
        segs.put("DS", "ds");
        reg8.put("AL", "cpu.r_al");
        reg8.put("ALr8b", "cpu.r_al");
        reg16.put("rAXr8", "cpu.r_eax");
        reg16.put("rBXr11", "cpu.r_ebx");
        reg16.put("rCXr9", "cpu.r_ecx");
        reg16.put("rDXr10", "cpu.r_edx");
        reg16.put("rDIr15", "cpu.r_edi");
        reg16.put("rBPr13", "cpu.r_ebp");
    }

    public static Operand get(String name, int opSize, boolean isMem)
    {
        if (name.equals("Ib"))
            return new Immediate(name, 8);
        if (name.equals("Iv"))
            return new Immediate(name, opSize);
        if (name.equals("Eb"))
        {
            if (isMem)
                return new Mem(name, 8);
            else
                return new Reg(name, 8);
        }
        if (name.equals("Ev"))
        {
            if (isMem)
                return new Mem(name, opSize);
            else
                return new Reg(name, opSize);
        }
        if (name.equals("Gv"))
            return new Reg(name, opSize);
        if (name.equals("Gb"))
            return new Reg(name, 8);
        if (name.equals("Jz") || name.equals("Jb"))
            return new Jump(name, opSize);
        if (name.equals("Ap"))
            return new FarPointer(name);
        if (name.equals("M"))
            return new Address(name);
        if (name.equals("S"))
            return new Segment(name);
        if (segs.containsKey(name))
            return new SpecificSegment(name, segs.get(name));
        if (reg8.containsKey(name))
            return new SpecificReg(name, reg8.get(name), 8);
        if (reg16.containsKey(name))
            return new SpecificReg(name, reg16.get(name), opSize);
        throw new IllegalStateException("Unknown operand "+name);
    }
}