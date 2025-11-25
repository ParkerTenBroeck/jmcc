rm -f a.out
rm -f out.prog
rm -f out.bin

set -e

mips-unknown-linux-gnu-gcc \
	-mips1 -mno-mips16 \
	-mno-check-zero-division \
	-mno-imadd \
        -fomit-frame-pointer \
	-mno-branch-likely \
	-Os \
	-nostdlib \
	-fno-builtin \
	-mabi=32 \
	-mplt \
	./src/* \
	-o ./a.out

#	-T link.map \

#-fno-delayed-branch


mips-unknown-linux-gnu-objdump -d a.out

entry=$(mips-unknown-linux-gnu-readelf -h a.out | grep "Entry point")
text=$(mips-unknown-linux-gnu-readelf -S a.out | grep .text)

mips-unknown-linux-gnu-objcopy \
	--only-section=.text \
	--only-section=.got \
	--only-section=.data \
	--only-section=.rodata \
	-O binary \
	a.out out.bin

java Script.java "$entry" "$text"


xxd out.bin

