package elf.word;

public record W64(long v) implements Word {

    @Override
    public String toString() {
        return ""+v;
    }

    @Override
    public long long_value() {
        return v;
    }

    public String to_str(){
        return ""+v;
    }
    public String to_str_hex(){
        return String.format("0x%016X", v);
    }
}
