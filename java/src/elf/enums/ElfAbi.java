package elf.enums;

import elf.exceptions.InvalidElfAbi;

public enum ElfAbi {
    SystemV(0x00),
    HP_UX(0x01),
    NetBSD(0x02),
    Linux(0x03),
    GNUHurd(0x04),
    Solaris(0x06),
    AIX(0x07),
    IRIX(0x08),
    FreeBSD(0x09),
    Tru64(0x0A),
    NovellModesto(0x0B),
    OpenBSD(0x0C),
    OpenVMS(0x0D),
    NonStopKernel(0x0E),
    AROS(0x0F),
    FenixOS(0x10),
    NuxiCloudABI(0x11),
    StratusTechnologiesOpenVOS(0x12);

    public final byte repr;

    ElfAbi(int repr) {
        this.repr = (byte)repr;
    }

    public static ElfAbi from_repr(byte repr) throws InvalidElfAbi {
        return switch (repr) {
            case 0x00 -> ElfAbi.SystemV;
            case 0x01 -> ElfAbi.HP_UX;
            case 0x02 -> ElfAbi.NetBSD;
            case 0x03 -> ElfAbi.Linux;
            case 0x04 -> ElfAbi.GNUHurd;
            case 0x06 -> ElfAbi.Solaris;
            case 0x07 -> ElfAbi.AIX;
            case 0x08 -> ElfAbi.IRIX;
            case 0x09 -> ElfAbi.FreeBSD;
            case 0x0A -> ElfAbi.Tru64;
            case 0x0B -> ElfAbi.NovellModesto;
            case 0x0C -> ElfAbi.OpenBSD;
            case 0x0D -> ElfAbi.OpenVMS;
            case 0x0E -> ElfAbi.NonStopKernel;
            case 0x0F -> ElfAbi.AROS;
            case 0x10 -> ElfAbi.FenixOS;
            case 0x11 -> ElfAbi.NuxiCloudABI;
            case 0x12 -> ElfAbi.StratusTechnologiesOpenVOS;
            default -> throw new InvalidElfAbi(repr);
        };
    }
}
