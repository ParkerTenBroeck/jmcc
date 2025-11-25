#include "stdlib.h"

// void print_int_custom(int value) {
//   char buf[32];
//   int len = 32;
//   int i = len;

//   unsigned int u;
//   if (value < 0) {
//     u = 1 + (unsigned int)(-(value + 1));
//   } else {
//     u = (unsigned int)value;
//   }

//   if (u == 0) {
//     buf[--i] = '0';
//   } else {
//     while (u != 0) {
//       unsigned int digit = u % 10;
//       buf[--i] = (char)('0' + digit);
//       u = u / 10;
//     }
//   }

//   if (value < 0) {
//     buf[--i] = '-';
//   }

//   print_str(&buf[i], len - i);
// }

// const char *text = "\nthis is ascii text\n";

// void meow(int value, int bottom) {
//   print_cstr("print int: ");
//   print_int(value * bottom);
//   print_cstr("\nprint custom int: ");
//   print_int_custom(value * bottom);
//   print_cstr("\n");
// }

__attribute__((noinline))
int shift_right(int value, int shift){
  return value >> shift;
}

int main() {
  print_int(shift_right(8, 2));
    printf("hello, %d meow\n", 23);
    // meow(12, 7);
    // print_cstr(text);

    char* string = alloc(50);
    for(int i = 0; i < 50; i ++){
        string[i] = 'a' + i;
    }
    free(string);
    print_str(string, 50);
    print_cstr("\n");
}
