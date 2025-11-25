package runtime;

import java.nio.ByteBuffer;

public class Memory {
    public int PAGE_SIZE_P2 = 16;
    public int PAGE_SIZE = 1<<PAGE_SIZE_P2;
    public int PAGE_MASK = PAGE_SIZE-1;

    private final ByteBuffer[] pages = new ByteBuffer[PAGE_SIZE];

    public void init(int page){
        pages[page] = ByteBuffer.allocate(PAGE_SIZE);
    }

    private ByteBuffer page_miss(int addr){
        init(addr>>>16);
        return pages[addr>>>16];
    }
    
    public void memcopy(byte[] data, int start, int size, int to){
        for(int i = 0; i < size; i ++){
            store_i8(to+i, data[i+start]);
        }
    }
    
    public void memcopy(int from, int size, int to){
        for(int i = 0; i < size; i ++){
            store_i8(to+i, load_i8(from+i));
        }
    }
    

    public byte load_i8(int addr){
        ByteBuffer page;
        try{
            page = pages[addr>>>16];
        }catch (IndexOutOfBoundsException e){
            page = page_miss(addr);
        }
        while(true){
            try{
                return page.get(addr & PAGE_MASK);
            }catch (NullPointerException e){
                page = page_miss(addr);
            }
        }
    }

    public int load_u8(int addr){
        ByteBuffer page;
        try{
            page = pages[addr>>>16];
        }catch (IndexOutOfBoundsException e){
            page = page_miss(addr);
        }
        while(true){
            try{
                return page.get(addr & PAGE_MASK) & 0xFF;
            }catch (NullPointerException e){
                page = page_miss(addr);
            }
        }
    }

    public short load_i16(int addr){
        ByteBuffer page;
        try{
            page = pages[addr>>>16];
        }catch (IndexOutOfBoundsException e){
            page = page_miss(addr);
        }
        while(true){
            try{
                return page.getShort(addr & PAGE_MASK);
            }catch (NullPointerException e){
                page = page_miss(addr);
            }
        }
    }

    public int load_u16(int addr){
        ByteBuffer page;
        try{
            page = pages[addr>>>16];
        }catch (IndexOutOfBoundsException e){
            page = page_miss(addr);
        }
        while(true){
            try{
                return page.getShort(addr & PAGE_MASK) & 0xFFFF;
            }catch (NullPointerException e){
                page = page_miss(addr);
            }
        }
    }

    public int load_i32(int addr){
        ByteBuffer page;
        try{
            page = pages[addr>>>16];
        }catch (IndexOutOfBoundsException e){
            page = page_miss(addr);
        }
        while(true){
            try{
                return page.getInt(addr & PAGE_MASK);
            }catch (NullPointerException e){
                page = page_miss(addr);
            }
        }
    }

    public long load_i64(int addr){
        ByteBuffer page;
        try{
            page = pages[addr>>>16];
        }catch (IndexOutOfBoundsException e){
            page = page_miss(addr);
        }
        while(true){
            try{
                return page.getLong(addr & PAGE_MASK);
            }catch (NullPointerException e){
                page = page_miss(addr);
            }
        }
    }

    public void store_i8(int addr, byte value){
        ByteBuffer page;
        try{
            page = pages[addr>>>16];
        }catch (IndexOutOfBoundsException e){
            page = page_miss(addr);
        }
        while(true){
            try{
                page.put(addr & PAGE_MASK, value);
                return;
            }catch (NullPointerException e){
                page = page_miss(addr);
            }
        }
    }

    public void store_i16(int addr, short value){
        ByteBuffer page;
        try{
            page = pages[addr>>>16];
        }catch (IndexOutOfBoundsException e){
            page = page_miss(addr);
        }
        while(true){
            try{
                page.putShort(addr & PAGE_MASK, value);
                return;
            }catch (NullPointerException e){
                page = page_miss(addr);
            }
        }
    }

    public void store_i32(int addr, int value){
        ByteBuffer page;
        try{
            page = pages[addr>>>16];
        }catch (IndexOutOfBoundsException e){
            page = page_miss(addr);
        }
        while(true){
            try{
                page.putInt(addr & PAGE_MASK, value);
                return;
            }catch (NullPointerException e){
                page = page_miss(addr);
            }
        }
    }

    public void store_i64(int addr, long value){
        ByteBuffer page;
        try{
            page = pages[addr>>>16];
        }catch (IndexOutOfBoundsException e){
            page = page_miss(addr);
        }
        while(true){
            try{
                page.putLong(addr & PAGE_MASK, value);
                return;
            }catch (NullPointerException e){
                page = page_miss(addr);
            }
        }
    }
}
