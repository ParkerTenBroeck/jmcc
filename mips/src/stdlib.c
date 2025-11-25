#include "stdlib.h"

#include "stdarg.h"

int strlen(const char *str) {
  int len = 0;
  while (*str != '\0') {
    len++;
    str++;
  }
  return len;
}


void print_cstr(const char *str) { print_str(str, strlen(str)); }

void print_hex(int value){
  print_cstr("0x");
  char buffer[8];
  for(int i = 0; i < 8; i ++){
    int nibble = (value>>((7-i)*4))&0xF;
    if(nibble>9){
      buffer[i] = 'A' + nibble-10;
    }else{
      buffer[i] = '0' + nibble;  
    }
  }
  print_str(buffer, 8);
}


void printf(const char* str, ...){
    va_list args;
    va_start(args, str);  

    int start = 0;
    int current = 0;
    while(str[current++]){
        if(str[current-1]!='%') continue;
        print_str(&str[start], current-start-1);

        switch(str[current++]){
            case 'd':{
                print_int(va_arg(args, int));
                break;
            }
            case 'p':{
                print_hex((int)va_arg(args, void*));
                break;
            }
            default: {}
        }
        start = current;
    }
    print_str(&str[start], current-start-1);
    va_end(args);
}


void* alloc(usize size){
    static usize bump = 0x80000000;
    void* ptr = (void*)(bump);
    bump += (size+7)&~0b111;
    printf("allocated: %p size: %d\n", ptr, size);
    return ptr;
}

void free(void*){}