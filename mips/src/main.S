
.section .text.start
_start:
.global _start

start:
.set noreorder
	.cpload $gp
	#la $sp, _sp

	jal main
	nop

	1:
	li $v0, 0
	syscall
	b 1b
	nop
.set reorder


.section .text


.global print_str
print_str:
	li $v0, 1
	syscall
	jr $ra


.global print_int
print_int:
	li $v0, 2
	syscall
	jr $ra
