import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PubgGunResourceGenerator {
    private static final int TEX_SIZE = 256;
    private static final List<Spec> SPECS = List.of(
        spec("id=groza;ko=그로자;en=Groza;type=rifle;profile=bullpup_ak;ammo=tacz:762x39;base=ak47;modes=auto,semi;rpm=750;mag=30;ext=40;damage=2.85;d2=2.35;d3=1.75;r1=28;r2=58;armor=0.3;head=1.55;speed=240;pierce=1;pellets=1;reloadTac=2.35;reloadEmpty=3.1;draw=0.34;aim=0.16;sprint=0.16;weight=3.1;moveAim=-0.18;pitch=0.95;yaw=0.5;aimAcc=0.18;standAcc=4.1;moveAcc=5.8;atts=scope,muzzle,extended_mag;cost=5;body=11;barrel=7;stock=2;metal=30343a;poly=202326;accent=5c6b50;wood=76523a;sort=60;bolt=closed_bolt;boltTime=0;burstCount=0;burstBpm=0;burstInterval=0;suppressed=false;heat=false"),
        spec("id=beryl_m762;ko=베릴 M762;en=Beryl M762;type=rifle;profile=ak;ammo=tacz:762x39;base=ak47;modes=auto,burst,semi;rpm=700;mag=30;ext=40;damage=2.65;d2=2.15;d3=1.65;r1=32;r2=64;armor=0.25;head=1.5;speed=240;pierce=1;pellets=1;reloadTac=2.35;reloadEmpty=3.1;draw=0.38;aim=0.18;sprint=0.18;weight=3.5;moveAim=-0.2;pitch=1.18;yaw=0.68;aimAcc=0.24;standAcc=4.7;moveAcc=6.5;atts=scope,muzzle,grip,extended_mag,laser;cost=4;body=10;barrel=10;stock=8;metal=34383d;poly=1d2024;accent=55606a;wood=694b32;sort=61;bolt=closed_bolt;boltTime=0;burstCount=3;burstBpm=750;burstInterval=0.35;suppressed=false;heat=false"),
        spec("id=ace32;ko=ACE32;en=ACE32;type=rifle;profile=ar;ammo=tacz:762x39;base=scar_h;modes=auto,semi;rpm=680;mag=30;ext=40;damage=2.55;d2=2.1;d3=1.65;r1=34;r2=68;armor=0.25;head=1.5;speed=240;pierce=1;pellets=1;reloadTac=2.25;reloadEmpty=2.9;draw=0.37;aim=0.16;sprint=0.17;weight=3.7;moveAim=-0.18;pitch=0.72;yaw=0.38;aimAcc=0.18;standAcc=3.7;moveAcc=5.4;atts=scope,muzzle,grip,stock,extended_mag,laser;cost=4;body=10;barrel=9;stock=8;metal=353a40;poly=1d2022;accent=69737a;wood=6c4e36;sort=62;bolt=closed_bolt;boltTime=0;burstCount=0;burstBpm=0;burstInterval=0;suppressed=false;heat=false"),
        spec("id=famas;ko=FAMAS;en=FAMAS;type=rifle;profile=bullpup_ar;ammo=tacz:556x45;base=aug;modes=auto,burst,semi;rpm=900;mag=25;ext=35;damage=2.35;d2=1.9;d3=1.45;r1=30;r2=60;armor=0.22;head=1.5;speed=295;pierce=1;pellets=1;reloadTac=2.05;reloadEmpty=2.55;draw=0.36;aim=0.15;sprint=0.15;weight=3.6;moveAim=-0.2;pitch=0.62;yaw=0.43;aimAcc=0.18;standAcc=3.9;moveAcc=5.7;atts=scope,muzzle,extended_mag;cost=5;body=12;barrel=8;stock=2;metal=34383e;poly=202428;accent=596168;wood=715039;sort=63;bolt=closed_bolt;boltTime=0;burstCount=3;burstBpm=900;burstInterval=0.3;suppressed=false;heat=false"),
        spec("id=k2;ko=K2;en=K2;type=rifle;profile=ar;ammo=tacz:556x45;base=m16a1;modes=auto,burst,semi;rpm=700;mag=30;ext=40;damage=2.35;d2=1.95;d3=1.5;r1=36;r2=72;armor=0.22;head=1.5;speed=295;pierce=1;pellets=1;reloadTac=2.3;reloadEmpty=3.1;draw=0.39;aim=0.17;sprint=0.17;weight=3.3;moveAim=-0.18;pitch=0.6;yaw=0.34;aimAcc=0.19;standAcc=3.8;moveAcc=5.5;atts=scope,muzzle,extended_mag;cost=3;body=10;barrel=10;stock=9;metal=34393d;poly=252a2c;accent=586454;wood=76533a;sort=64;bolt=closed_bolt;boltTime=0;burstCount=3;burstBpm=800;burstInterval=0.34;suppressed=false;heat=false"),
        spec("id=mk47_mutant;ko=Mk47 뮤턴트;en=Mk47 Mutant;type=rifle;profile=ar;ammo=tacz:762x39;base=sks_tactical;modes=burst,semi;rpm=800;mag=20;ext=30;damage=2.95;d2=2.45;d3=1.9;r1=42;r2=82;armor=0.3;head=1.65;speed=260;pierce=1;pellets=1;reloadTac=2.25;reloadEmpty=2.9;draw=0.4;aim=0.17;sprint=0.18;weight=3.6;moveAim=-0.2;pitch=0.9;yaw=0.42;aimAcc=0.16;standAcc=4.2;moveAcc=5.9;atts=scope,muzzle,grip,extended_mag,laser;cost=4;body=11;barrel=11;stock=8;metal=373b40;poly=24282b;accent=735d44;wood=7a5a3c;sort=65;bolt=closed_bolt;boltTime=0;burstCount=2;burstBpm=800;burstInterval=0.1;suppressed=false;heat=false"),
        spec("id=mini14;ko=미니14;en=Mini14;type=rifle;profile=dmr;ammo=tacz:556x45;base=spr15hb;modes=semi;rpm=600;mag=20;ext=30;damage=2.85;d2=2.45;d3=2.05;r1=58;r2=115;armor=0.25;head=1.8;speed=330;pierce=1;pellets=1;reloadTac=2.05;reloadEmpty=2.55;draw=0.42;aim=0.15;sprint=0.18;weight=3.2;moveAim=-0.25;pitch=0.62;yaw=0.28;aimAcc=0.1;standAcc=3.2;moveAcc=4.8;atts=scope,muzzle,extended_mag;cost=3;body=11;barrel=14;stock=9;metal=41464c;poly=25292c;accent=69727a;wood=7b5b3e;sort=66;bolt=closed_bolt;boltTime=0;burstCount=0;burstBpm=0;burstInterval=0;suppressed=false;heat=false"),
        spec("id=mk12;ko=Mk12;en=Mk12;type=rifle;profile=dmr;ammo=tacz:556x45;base=spr15hb;modes=semi;rpm=600;mag=20;ext=30;damage=3.1;d2=2.6;d3=2.15;r1=62;r2=125;armor=0.28;head=1.85;speed=310;pierce=1;pellets=1;reloadTac=2.2;reloadEmpty=2.75;draw=0.45;aim=0.16;sprint=0.19;weight=4;moveAim=-0.28;pitch=0.72;yaw=0.3;aimAcc=0.09;standAcc=3.4;moveAcc=5;atts=scope,muzzle,grip,extended_mag;cost=4;body=11;barrel=15;stock=8;metal=353a3f;poly=202428;accent=6d735f;wood=76533c;sort=67;bolt=closed_bolt;boltTime=0;burstCount=0;burstBpm=0;burstInterval=0;suppressed=false;heat=false"),
        spec("id=vss;ko=VSS;en=VSS;type=rifle;profile=vss;ammo=tacz:9mm;base=sks_tactical;modes=auto,semi;rpm=700;mag=10;ext=20;damage=2.45;d2=2.05;d3=1.55;r1=26;r2=52;armor=0.2;head=1.8;speed=165;pierce=1;pellets=1;reloadTac=2.1;reloadEmpty=2.6;draw=0.4;aim=0.18;sprint=0.18;weight=2.6;moveAim=-0.18;pitch=0.46;yaw=0.24;aimAcc=0.1;standAcc=3.3;moveAcc=4.7;atts=stock,extended_mag;cost=4;body=10;barrel=14;stock=10;metal=30353a;poly=1f2225;accent=4b5247;wood=79563b;sort=68;bolt=closed_bolt;boltTime=0;burstCount=0;burstBpm=0;burstInterval=0;suppressed=true;heat=false"),
        spec("id=dragunov;ko=드라구노프;en=Dragunov;type=rifle;profile=dmr_wood;ammo=tacz:762x54;base=slr;modes=semi;rpm=300;mag=10;ext=20;damage=4.05;d2=3.4;d3=2.7;r1=65;r2=125;armor=0.45;head=2.05;speed=300;pierce=2;pellets=1;reloadTac=2.45;reloadEmpty=3.05;draw=0.48;aim=0.2;sprint=0.21;weight=4.3;moveAim=-0.3;pitch=1.45;yaw=0.55;aimAcc=0.09;standAcc=4.3;moveAcc=6.2;atts=scope,muzzle,extended_mag,stock;cost=5;body=12;barrel=16;stock=10;metal=32373b;poly=202326;accent=5f5e4d;wood=8a603e;sort=69;bolt=closed_bolt;boltTime=0;burstCount=0;burstBpm=0;burstInterval=0;suppressed=false;heat=false"),
        spec("id=tommy_gun;ko=토미 건;en=Tommy Gun;type=smg;profile=smg_wood;ammo=tacz:45acp;base=ump45;modes=auto,semi;rpm=700;mag=30;ext=50;damage=2.35;d2=1.95;d3=1.45;r1=26;r2=52;armor=0.15;head=1.3;speed=185;pierce=1;pellets=1;reloadTac=2.35;reloadEmpty=2.95;draw=0.44;aim=0.14;sprint=0.12;weight=4.7;moveAim=-0.1;pitch=0.58;yaw=0.36;aimAcc=0.25;standAcc=3.2;moveAcc=4.8;atts=scope,muzzle,grip,extended_mag;cost=3;body=10;barrel=10;stock=10;metal=34383b;poly=25282a;accent=62543f;wood=8b5f38;sort=70;bolt=closed_bolt;boltTime=0;burstCount=0;burstBpm=0;burstInterval=0;suppressed=false;heat=false"),
        spec("id=mp9;ko=MP9;en=MP9;type=smg;profile=smg_compact;ammo=tacz:9mm;base=mp5k;modes=auto,semi;rpm=1000;mag=30;ext=40;damage=1.85;d2=1.45;d3=1.05;r1=18;r2=38;armor=0.15;head=1.25;speed=185;pierce=1;pellets=1;reloadTac=1.95;reloadEmpty=2.45;draw=0.27;aim=0.09;sprint=0.08;weight=1.4;moveAim=-0.05;pitch=0.36;yaw=0.28;aimAcc=0.26;standAcc=2.8;moveAcc=4.2;atts=scope,muzzle,extended_mag,laser;cost=3;body=7;barrel=5;stock=7;metal=353a3e;poly=202326;accent=666d72;wood=76533a;sort=71;bolt=closed_bolt;boltTime=0;burstCount=0;burstBpm=0;burstInterval=0;suppressed=false;heat=false"),
        spec("id=js9;ko=JS9;en=JS9;type=smg;profile=bullpup_smg;ammo=tacz:9mm;base=p90;modes=auto,semi;rpm=900;mag=30;ext=40;damage=2.05;d2=1.7;d3=1.25;r1=24;r2=50;armor=0.2;head=1.3;speed=200;pierce=1;pellets=1;reloadTac=2.1;reloadEmpty=2.6;draw=0.33;aim=0.11;sprint=0.1;weight=2.9;moveAim=-0.08;pitch=0.42;yaw=0.24;aimAcc=0.2;standAcc=2.9;moveAcc=4.3;atts=scope,muzzle,extended_mag;cost=4;body=10;barrel=7;stock=3;metal=353a3f;poly=1d2226;accent=56646b;wood=75523a;sort=72;bolt=closed_bolt;boltTime=0;burstCount=0;burstBpm=0;burstInterval=0;suppressed=false;heat=false"),
        spec("id=win94;ko=Win94;en=Win94;type=sniper;profile=lever;ammo=tacz:45acp;base=springfield1873;modes=semi;rpm=90;mag=8;ext=0;damage=6.6;d2=5.2;d3=4.1;r1=48;r2=95;armor=0.35;head=2.05;speed=235;pierce=1;pellets=1;reloadTac=2.4;reloadEmpty=3.1;draw=0.43;aim=0.16;sprint=0.18;weight=3.1;moveAim=-0.35;pitch=3.2;yaw=0.72;aimAcc=0.15;standAcc=3.4;moveAcc=5;atts=;cost=3;body=11;barrel=16;stock=11;metal=3a3b3a;poly=262626;accent=5d503b;wood=8c6039;sort=73;bolt=manual_action;boltTime=0.65;burstCount=0;burstBpm=0;burstInterval=0;suppressed=false;heat=false"),
        spec("id=m24;ko=M24;en=M24;type=sniper;profile=sniper;ammo=tacz:308;base=m700;modes=semi;rpm=42;mag=5;ext=7;damage=8.4;d2=7.05;d3=5.6;r1=68;r2=138;armor=0.55;head=2.2;speed=300;pierce=3;pellets=1;reloadTac=2.9;reloadEmpty=3.7;draw=0.52;aim=0.18;sprint=0.22;weight=5.4;moveAim=-0.45;pitch=4.5;yaw=0.75;aimAcc=0.075;standAcc=4.2;moveAcc=6.2;atts=scope,muzzle,extended_mag,stock;cost=5;body=12;barrel=18;stock=11;metal=383d42;poly=232629;accent=657079;wood=78563b;sort=74;bolt=manual_action;boltTime=1.05;burstCount=0;burstBpm=0;burstInterval=0;suppressed=false;heat=false"),
        spec("id=s12k;ko=S12K;en=S12K;type=shotgun;profile=shotgun_mag;ammo=tacz:12g;base=m1014;modes=semi;rpm=240;mag=5;ext=8;damage=7.6;d2=5.7;d3=3.8;r1=11;r2=23;armor=0.25;head=1.33;speed=155;pierce=1;pellets=9;reloadTac=1.9;reloadEmpty=2.3;draw=0.36;aim=0.14;sprint=0.11;weight=3.5;moveAim=-0.15;pitch=4.7;yaw=1.15;aimAcc=3.4;standAcc=3.7;moveAcc=4;atts=scope,muzzle,extended_mag;cost=4;body=11;barrel=14;stock=8;metal=33383c;poly=1e2225;accent=566069;wood=76543b;sort=75;bolt=closed_bolt;boltTime=0;burstCount=0;burstBpm=0;burstInterval=0;suppressed=false;heat=false"),
        spec("id=dbs;ko=DBS;en=DBS;type=shotgun;profile=shotgun_bullpup;ammo=tacz:12g;base=m870;modes=burst;rpm=480;mag=14;ext=0;damage=8.2;d2=6.1;d3=4;r1=13;r2=26;armor=0.3;head=1.33;speed=160;pierce=1;pellets=9;reloadTac=4.8;reloadEmpty=6.1;draw=0.45;aim=0.16;sprint=0.13;weight=4.2;moveAim=-0.18;pitch=4.2;yaw=1.05;aimAcc=3;standAcc=3.3;moveAcc=3.7;atts=scope;cost=5;body=13;barrel=10;stock=3;metal=34393e;poly=202428;accent=5d666b;wood=77553b;sort=76;bolt=closed_bolt;boltTime=0;burstCount=2;burstBpm=480;burstInterval=0.45;suppressed=false;heat=false"),
        spec("id=o12;ko=O12;en=O12;type=shotgun;profile=shotgun_drum;ammo=tacz:12g;base=aa12;modes=auto,burst,semi;rpm=480;mag=30;ext=0;damage=6.2;d2=5.1;d3=3.8;r1=48;r2=96;armor=0.3;head=1.5;speed=310;pierce=2;pellets=1;reloadTac=2.6;reloadEmpty=3.2;draw=0.48;aim=0.14;sprint=0.14;weight=4;moveAim=-0.16;pitch=1.05;yaw=0.55;aimAcc=0.18;standAcc=3.1;moveAcc=4.6;atts=scope,muzzle,grip;cost=5;body=11;barrel=11;stock=6;metal=30363b;poly=1d2226;accent=69747d;wood=76543a;sort=77;bolt=closed_bolt;boltTime=0;burstCount=3;burstBpm=720;burstInterval=0.25;suppressed=false;heat=false"),
        spec("id=mg3;ko=MG3;en=MG3;type=mg;profile=lmg_belt;ammo=tacz:308;base=m249;modes=auto,burst;rpm=660;mag=75;ext=0;damage=3.05;d2=2.55;d3=1.95;r1=42;r2=86;armor=0.4;head=1.5;speed=275;pierce=2;pellets=1;reloadTac=4.4;reloadEmpty=5.2;draw=0.95;aim=0.24;sprint=0.28;weight=11.5;moveAim=-0.42;pitch=0.88;yaw=0.48;aimAcc=0.25;standAcc=4.6;moveAcc=6;atts=scope;cost=5;body=13;barrel=18;stock=11;metal=34383b;poly=242729;accent=5e666b;wood=76543b;sort=78;bolt=closed_bolt;boltTime=0;burstCount=75;burstBpm=990;burstInterval=0.05;suppressed=false;heat=true"),
        spec("id=rpd;ko=RPD;en=RPD;type=mg;profile=lmg_rpd;ammo=tacz:762x39;base=rpk;modes=auto;rpm=750;mag=50;ext=110;damage=3.1;d2=2.55;d3=1.95;r1=45;r2=92;armor=0.35;head=1.5;speed=245;pierce=2;pellets=1;reloadTac=4.6;reloadEmpty=5.2;draw=0.8;aim=0.21;sprint=0.24;weight=7.4;moveAim=-0.28;pitch=0.7;yaw=0.4;aimAcc=0.22;standAcc=4.1;moveAcc=5.6;atts=scope,extended_mag;cost=5;body=13;barrel=17;stock=11;metal=363a3d;poly=242728;accent=625a49;wood=8a603b;sort=79;bolt=closed_bolt;boltTime=0;burstCount=0;burstBpm=0;burstInterval=0;suppressed=false;heat=true"),
        spec("id=skorpion;ko=스콜피온;en=Skorpion;type=pistol;profile=machine_pistol;ammo=tacz:9mm;base=b93r;modes=auto,semi;rpm=850;mag=20;ext=40;damage=1.45;d2=1.15;d3=0.85;r1=16;r2=34;armor=0.1;head=1.3;speed=170;pierce=0;pellets=1;reloadTac=1.55;reloadEmpty=1.95;draw=0.22;aim=0.08;sprint=0.05;weight=1.3;moveAim=0;pitch=0.38;yaw=0.3;aimAcc=0.28;standAcc=2.5;moveAcc=3.6;atts=scope,muzzle,grip,stock,extended_mag,laser;cost=2;body=6;barrel=4;stock=5;metal=363b3f;poly=202427;accent=606970;wood=76543a;sort=80;bolt=closed_bolt;boltTime=0;burstCount=0;burstBpm=0;burstInterval=0;suppressed=false;heat=false"),
        spec("id=r1895;ko=R1895;en=R1895;type=pistol;profile=revolver;ammo=tacz:762x25;base=rhino357;modes=semi;rpm=150;mag=7;ext=0;damage=3.75;d2=3.05;d3=2.3;r1=22;r2=45;armor=0.35;head=1.75;speed=190;pierce=1;pellets=1;reloadTac=2.8;reloadEmpty=3.8;draw=0.3;aim=0.13;sprint=0.08;weight=0.85;moveAim=-0.05;pitch=1.8;yaw=0.7;aimAcc=0.17;standAcc=2.8;moveAcc=4;atts=muzzle;cost=2;body=5;barrel=8;stock=4;metal=45484a;poly=272829;accent=6b6253;wood=875b38;sort=81;bolt=closed_bolt;boltTime=0;burstCount=0;burstBpm=0;burstInterval=0;suppressed=false;heat=false")
    );

    private PubgGunResourceGenerator() {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: java tools/PubgGunResourceGenerator.java <output-directory>");
        }

        Path output = Path.of(args[0]).toAbsolutePath().normalize();
        clearDirectory(output);
        Files.createDirectories(output);

        for (Spec spec : SPECS) {
            generate(output, spec);
        }
    }

    private static Spec spec(String line) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String part : line.split(";")) {
            int split = part.indexOf('=');
            if (split <= 0) {
                throw new IllegalArgumentException("Invalid gun spec part: " + part);
            }
            values.put(part.substring(0, split), part.substring(split + 1));
        }
        return new Spec(values);
    }

    private static void generate(Path output, Spec spec) throws Exception {
        Path pack = output.resolve("assets/tacz/custom/tacz_default_gun");
        write(pack.resolve("data/tacz/index/guns/" + spec.s("id") + ".json"), indexJson(spec));
        write(pack.resolve("data/tacz/data/guns/" + spec.s("id") + "_data.json"), dataJson(spec));
        write(pack.resolve("data/tacz/recipe/gun/" + spec.s("id") + ".json"), recipeJson(spec));
        write(pack.resolve("data/tacz/tacz_tags/attachments/allow_attachments/" + spec.s("id") + ".json"), attachmentTagJson(spec));
        write(pack.resolve("assets/tacz/display/guns/" + spec.s("id") + "_display.json"), displayJson(spec));

        Model model = createModel(spec);
        write(pack.resolve("assets/tacz/geo_models/gun/" + spec.s("id") + "_geo.json"), geometryJson(spec, model, false));
        write(pack.resolve("assets/tacz/geo_models/gun/lod/" + spec.s("id") + ".json"), geometryJson(spec, model, true));

        renderTexture(spec, pack.resolve("assets/tacz/textures/gun/uv/" + spec.s("id") + ".png"), TextureKind.COLOR);
        renderTexture(spec, pack.resolve("assets/tacz/textures/gun/uv/" + spec.s("id") + "_n.png"), TextureKind.NORMAL);
        renderTexture(spec, pack.resolve("assets/tacz/textures/gun/uv/" + spec.s("id") + "_s.png"), TextureKind.SPECULAR);
        renderTexture(spec, pack.resolve("assets/tacz/textures/gun/lod/" + spec.s("id") + ".png"), TextureKind.COLOR);
        renderIcon(spec, model, pack.resolve("assets/tacz/textures/gun/slot/" + spec.s("id") + ".png"), 256, 96, false);
        renderIcon(spec, model, pack.resolve("assets/tacz/textures/gun/hud/" + spec.s("id") + ".png"), 256, 64, true);
    }

    private static String indexJson(Spec s) {
        return "{\n"
                + "  \"name\": \"tacz.gun." + s.s("id") + ".name\",\n"
                + "  \"tooltip\": \"tacz.gun." + s.s("id") + ".desc\",\n"
                + "  \"display\": \"tacz:" + s.s("id") + "_display\",\n"
                + "  \"data\": \"tacz:" + s.s("id") + "_data\",\n"
                + "  \"type\": \"" + s.s("type") + "\",\n"
                + "  \"item_type\": \"modern_kinetic\",\n"
                + "  \"sort\": " + s.i("sort") + "\n"
                + "}\n";
    }

    private static String dataJson(Spec s) {
        String id = s.s("id");
        StringBuilder out = new StringBuilder();
        out.append("{\n");
        out.append("  \"ammo\": \"").append(s.s("ammo")).append("\",\n");
        out.append("  \"ammo_amount\": ").append(s.i("mag")).append(",\n");
        if (s.i("ext") > 0) {
            out.append("  \"extended_mag_ammo_amount\": [")
                    .append(s.i("ext")).append(", ").append(s.i("ext")).append(", ").append(s.i("ext")).append("],\n");
        }
        out.append("  \"bolt\": \"").append(s.s("bolt")).append("\",\n");
        out.append("  \"rpm\": ").append(s.i("rpm")).append(",\n");
        if ("manual_action".equals(s.s("bolt")) && s.d("boltTime") > 0) {
            out.append("  \"bolt_action_time\": ").append(n(s.d("boltTime"))).append(",\n");
            out.append("  \"bolt_feed_time\": ").append(n(s.d("boltTime") * 0.55)).append(",\n");
        }

        out.append("  \"bullet\": {\n");
        out.append("    \"life\": ").append(s.s("type").equals("sniper") ? "1.1" : s.s("type").equals("shotgun") ? "0.65" : "0.9").append(",\n");
        out.append("    \"bullet_amount\": ").append(s.i("pellets")).append(",\n");
        out.append("    \"damage\": ").append(n(s.d("damage"))).append(",\n");
        out.append("    \"tracer_count_interval\": 0,\n");
        out.append("    \"extra_damage\": {\n");
        out.append("      \"armor_ignore\": ").append(n(s.d("armor"))).append(",\n");
        out.append("      \"head_shot_multiplier\": ").append(n(s.d("head"))).append(",\n");
        out.append("      \"damage_adjust\": [\n");
        out.append("        {\"distance\": ").append(s.i("r1")).append(", \"damage\": ").append(n(s.d("damage"))).append("},\n");
        out.append("        {\"distance\": ").append(s.i("r2")).append(", \"damage\": ").append(n(s.d("d2"))).append("},\n");
        out.append("        {\"distance\": \"infinite\", \"damage\": ").append(n(s.d("d3"))).append("}\n");
        out.append("      ]\n");
        out.append("    },\n");
        out.append("    \"speed\": ").append(s.i("speed")).append(",\n");
        out.append("    \"gravity\": 0.15,\n");
        out.append("    \"knockback\": ").append(s.s("type").equals("shotgun") ? "0.15" : "0").append(",\n");
        out.append("    \"friction\": ").append(s.s("type").equals("shotgun") ? "0.05" : "0.02").append(",\n");
        out.append("    \"ignite\": false,\n");
        out.append("    \"pierce\": ").append(s.i("pierce")).append("\n");
        out.append("  },\n");

        out.append("  \"reload\": {\n");
        out.append("    \"type\": \"magazine\",\n");
        out.append("    \"feed\": {\"empty\": ").append(n(s.d("reloadEmpty") * .74))
                .append(", \"tactical\": ").append(n(s.d("reloadTac") * .72)).append("},\n");
        out.append("    \"cooldown\": {\"empty\": ").append(n(s.d("reloadEmpty")))
                .append(", \"tactical\": ").append(n(s.d("reloadTac"))).append("}\n");
        out.append("  },\n");

        out.append("  \"draw_time\": ").append(n(s.d("draw"))).append(",\n");
        out.append("  \"put_away_time\": ").append(n(Math.max(.2, s.d("draw") + .08))).append(",\n");
        out.append("  \"aim_time\": ").append(n(s.d("aim"))).append(",\n");
        out.append("  \"sprint_time\": ").append(n(s.d("sprint"))).append(",\n");
        out.append("  \"weight\": ").append(n(s.d("weight"))).append(",\n");
        out.append("  \"movement_speed\": {\"base\": 0.0, \"aim\": ").append(n(s.d("moveAim")))
                .append(", \"reload\": ").append(s.s("type").equals("pistol") ? "0.0" : "-0.1").append("},\n");

        out.append("  \"fire_mode\": [");
        String[] modes = s.s("modes").split(",");
        for (int i = 0; i < modes.length; i++) {
            if (i > 0) out.append(", ");
            out.append("\"").append(modes[i]).append("\"");
        }
        out.append("],\n");

        if (s.i("burstCount") > 0) {
            out.append("  \"burst_data\": {\n");
            out.append("    \"continuous_shoot\": ").append(s.i("burstCount") >= 20 ? "true" : "false").append(",\n");
            out.append("    \"count\": ").append(s.i("burstCount")).append(",\n");
            out.append("    \"bpm\": ").append(s.i("burstBpm")).append(",\n");
            out.append("    \"min_interval\": ").append(n(s.d("burstInterval"))).append("\n");
            out.append("  },\n");
        }

        if (s.b("heat")) {
            out.append("  \"heat\": {\n");
            out.append("    \"max\": 420, \"per_shot\": 2, \"cooling_multiplier\": 12, \"cooling_delay\": 600,\n");
            out.append("    \"over_heat_time\": 2200, \"min_inaccuracy\": 1.0, \"max_inaccuracy\": 1.2,\n");
            out.append("    \"min_rpm_mod\": 1.0, \"max_rpm_mod\": 0.88\n");
            out.append("  },\n");
        }

        out.append("  \"crawl_recoil_multiplier\": 0.5,\n");
        out.append("  \"recoil\": {\n");
        out.append("    \"pitch\": [\n");
        out.append("      {\"time\": 0, \"value\": [").append(n(s.d("pitch") * .92)).append(", ").append(n(s.d("pitch"))).append("]},\n");
        out.append("      {\"time\": 0.12, \"value\": [").append(n(s.d("pitch") * .78)).append(", ").append(n(s.d("pitch") * .92)).append("]},\n");
        out.append("      {\"time\": 0.45, \"value\": [").append(n(-s.d("pitch") * .22)).append(", ").append(n(-s.d("pitch") * .18)).append("]},\n");
        out.append("      {\"time\": 0.62, \"value\": [0, 0]}\n");
        out.append("    ],\n");
        out.append("    \"yaw\": [\n");
        out.append("      {\"time\": 0, \"value\": [").append(n(-s.d("yaw"))).append(", ").append(n(s.d("yaw"))).append("]},\n");
        out.append("      {\"time\": 0.34, \"value\": [0, 0]}\n");
        out.append("    ]\n");
        out.append("  },\n");

        out.append("  \"inaccuracy\": {\n");
        out.append("    \"stand\": ").append(n(s.d("standAcc"))).append(",\n");
        out.append("    \"move\": ").append(n(s.d("moveAcc"))).append(",\n");
        out.append("    \"sneak\": ").append(n(Math.max(.8, s.d("standAcc") * .48))).append(",\n");
        out.append("    \"lie\": ").append(n(Math.max(.6, s.d("standAcc") * .32))).append(",\n");
        out.append("    \"aim\": ").append(n(s.d("aimAcc"))).append(",\n");
        out.append("    \"run\": ").append(n(s.d("standAcc") * 2.0)).append(",\n");
        out.append("    \"fly\": ").append(n(s.d("standAcc") * 4.0)).append("\n");
        out.append("  },\n");

        out.append("  \"melee\": {\n");
        out.append("    \"distance\": 1, \"cooldown\": 0.7,\n");
        out.append("    \"default\": {\n");
        out.append("      \"animation_type\": \"").append(s.s("type").equals("pistol") ? "melee_push" : "melee_stock").append("\",\n");
        out.append("      \"distance\": 1, \"range_angle\": 35, \"damage\": ")
                .append(s.s("type").equals("pistol") ? "0.3" : "0.45")
                .append(", \"knockback\": 0.5, \"prep\": 0.1\n");
        out.append("    }\n");
        out.append("  }");

        List<String> atts = csv(s.s("atts"));
        if (!atts.isEmpty()) {
            out.append(",\n  \"allow_attachment_types\": [");
            for (int i = 0; i < atts.size(); i++) {
                if (i > 0) out.append(", ");
                out.append("\"").append(atts.get(i)).append("\"");
            }
            out.append("]");
        }
        if (!s.s("type").equals("pistol")) {
            out.append(",\n  \"can_crawl\": true,\n  \"can_slide\": ").append(s.d("weight") < 5 ? "true" : "false");
        }
        out.append("\n}\n");
        return out.toString();
    }

    private static String recipeJson(Spec s) {
        int cost = s.i("cost");
        int iron = 30 + cost * 13;
        int copper = 4 + cost * 2;
        int gold = 4 + cost * 4;
        int coal = 8 + cost * 2;
        int emerald = 2 + cost;
        int quartz = 4 + cost * 2;
        int diamond = Math.max(0, cost - 3);

        StringBuilder out = new StringBuilder();
        out.append("{\n  \"materials\": [\n");
        out.append("    {\"item\": \"#c:ingots/iron\", \"count\": ").append(iron).append("},\n");
        out.append("    {\"item\": \"#c:ingots/copper\", \"count\": ").append(copper).append("},\n");
        out.append("    {\"item\": \"#c:ingots/gold\", \"count\": ").append(gold).append("},\n");
        out.append("    {\"item\": \"minecraft:coal\", \"count\": ").append(coal).append("},\n");
        out.append("    {\"item\": \"#c:gems/emerald\", \"count\": ").append(emerald).append("},\n");
        out.append("    {\"item\": \"#c:gems/quartz\", \"count\": ").append(quartz).append("}");
        if (diamond > 0) {
            out.append(",\n    {\"item\": \"#c:gems/diamond\", \"count\": ").append(diamond).append("}");
        }
        out.append("\n  ],\n");
        out.append("  \"result\": {\"type\": \"gun\", \"id\": \"tacz:").append(s.s("id")).append("\"},\n");
        out.append("  \"type\": \"tacz:gun_smith_table_crafting\"\n}\n");
        return out.toString();
    }

    private static String attachmentTagJson(Spec s) {
        Set<String> tags = new LinkedHashSet<>();
        List<String> atts = csv(s.s("atts"));
        String type = s.s("type");

        for (String att : atts) {
            switch (att) {
                case "scope" -> {
                    if ("pistol".equals(type)) {
                        tags.add("#tacz:pistol_sight");
                    } else if ("sniper".equals(type)) {
                        tags.add("#tacz:scope");
                        tags.add("#tacz:scope_lowsight");
                    } else {
                        tags.add("#tacz:scope_sight");
                        tags.add("#tacz:scope_scope");
                    }
                }
                case "muzzle" -> {
                    if ("pistol".equals(type)) tags.add("#tacz:pistol_muzzle");
                    else if ("shotgun".equals(type)) tags.add("#tacz:muzzle_shotgun");
                    else tags.add("#tacz:muzzle");
                }
                case "extended_mag" -> {
                    if ("pistol".equals(type) || "smg".equals(type)) tags.add("#tacz:light_extended_mag");
                    else if ("sniper".equals(type)) tags.add("#tacz:sniper_extended_mag");
                    else if ("shotgun".equals(type)) tags.add("#tacz:shotgun_extended_mag");
                    else tags.add("#tacz:extended_mag");
                }
                case "stock" -> {
                    tags.add("#tacz:stock");
                    tags.add("#tacz:oem_stock");
                }
                case "grip" -> tags.add("#tacz:grip");
                case "laser" -> tags.add("pistol".equals(type) || "smg".equals(type) ? "#tacz:pistol_laser" : "#tacz:ar_laser");
                default -> throw new IllegalArgumentException("Unknown attachment type: " + att);
            }
        }

        if (!"shotgun".equals(type) && !"vss".equals(s.s("id"))) {
            tags.add("#tacz:ammo_mod_no_he");
        }

        StringBuilder out = new StringBuilder("[\n");
        int index = 0;
        for (String tag : tags) {
            if (index++ > 0) out.append(",\n");
            out.append("  \"").append(tag).append("\"");
        }
        out.append("\n]\n");
        return out.toString();
    }

    private static String displayJson(Spec s) throws IOException {
        String base = s.s("base");
        String id = s.s("id");
        Path source = Path.of("src/main/resources/assets/tacz/custom/tacz_default_gun/assets/tacz/display/guns/" + base + "_display.json");
        String value = Files.readString(source);

        value = value.replace("tacz:gun/" + base + "_geo", "tacz:gun/" + id + "_geo");
        value = value.replace("tacz:gun/uv/" + base, "tacz:gun/uv/" + id);
        value = value.replace("tacz:gun/lod/" + base, "tacz:gun/lod/" + id);
        value = replaceJsonStringValue(value, "hud", "tacz:gun/hud/" + id);
        value = replaceJsonStringValue(value, "slot", "tacz:gun/slot/" + id);

        if (s.b("suppressed")) {
            String silence = jsonStringValue(value, "silence");
            String silence3p = jsonStringValue(value, "silence_3p");
            if (silence != null) value = replaceJsonStringValue(value, "shoot", silence);
            if (silence3p != null) value = replaceJsonStringValue(value, "shoot_3p", silence3p);
        }

        String layers = soundLayersJson(s);
        int closingBrace = value.lastIndexOf('}');
        if (closingBrace < 0) {
            throw new IllegalArgumentException("Display JSON has no closing object: " + id);
        }
        return value.substring(0, closingBrace).stripTrailing()
                + ",\n  \"sound_layers\": " + layers + "\n}\n";
    }

    private static String soundLayersJson(Spec s) {
        SoundProfile profile = soundProfile(s.s("id"));
        return "{\n"
                + soundLayerEntry("shoot", profile.shoot()) + ",\n"
                + soundLayerEntry("shoot_3p", profile.shoot3p()) + ",\n"
                + soundLayerEntry("silence", profile.silence()) + ",\n"
                + soundLayerEntry("silence_3p", profile.silence3p()) + "\n"
                + "  }";
    }

    private static String soundLayerEntry(String name, List<SoundLayerSpec> layers) {
        StringBuilder out = new StringBuilder();
        out.append("    \"").append(name).append("\": [");
        for (int i = 0; i < layers.size(); i++) {
            SoundLayerSpec layer = layers.get(i);
            if (i > 0) out.append(", ");
            out.append("{\"sound\": \"").append(layer.sound()).append("\", \"volume\": ")
                    .append(n(layer.volume())).append(", \"pitch\": ").append(n(layer.pitch())).append("}");
        }
        out.append("]");
        return out.toString();
    }

    private static SoundProfile soundProfile(String id) {
        return switch (id) {
            case "groza" -> profile(
                    layers(layer("tacz:scar_h/scar_h_shoot", .28, .88), layer("tacz:rpk/rpk_shoot", .14, .82)),
                    layers(layer("tacz:scar_h/scar_h_shoot_3p", .36, .90), layer("tacz:rpk/rpk_shoot_3p", .18, .84)),
                    layers(layer("tacz:scar_h/scar_h_silence", .24, .88)),
                    layers(layer("tacz:scar_h/scar_h_silence_3p", .30, .90)));
            case "beryl_m762" -> profile(
                    layers(layer("tacz:rpk/rpk_shoot", .25, .94), layer("tacz:scar_h/scar_h_shoot", .12, .97)),
                    layers(layer("tacz:rpk/rpk_shoot_3p", .34, .94), layer("tacz:scar_h/scar_h_shoot_3p", .16, .96)),
                    layers(layer("tacz:rpk/rpk_silence", .22, .94)),
                    layers(layer("tacz:rpk/rpk_silence_3p", .28, .94)));
            case "ace32" -> profile(
                    layers(layer("tacz:m416/m416_shoot", .22, .88), layer("tacz:scar_h/scar_h_shoot", .10, .98)),
                    layers(layer("tacz:m416/m416_shoot_3p", .28, .90), layer("tacz:scar_h/scar_h_shoot_3p", .14, .98)),
                    layers(layer("tacz:m416/m416_silence", .20, .90)),
                    layers(layer("tacz:m416/m416_silence_3p", .26, .90)));
            case "famas" -> profile(
                    layers(layer("tacz:m416/m416_shoot", .24, 1.07), layer("tacz:mp5k/mp5k_shoot", .12, 1.02)),
                    layers(layer("tacz:m416/m416_shoot_3p", .30, 1.05), layer("tacz:mp5k/mp5k_shoot_3p", .15, 1.02)),
                    layers(layer("tacz:m416/m416_silence", .20, 1.06)),
                    layers(layer("tacz:m416/m416_silence_3p", .25, 1.04)));
            case "k2" -> profile(
                    layers(layer("tacz:m416/m416_shoot", .20, .97), layer("tacz:aug/aug_shoot", .12, 1.00)),
                    layers(layer("tacz:m416/m416_shoot_3p", .27, .97), layer("tacz:aug/aug_shoot_3p", .14, 1.00)),
                    layers(layer("tacz:m416/m416_silence", .18, .98)),
                    layers(layer("tacz:m416/m416_silence_3p", .24, .98)));
            case "mk47_mutant" -> profile(
                    layers(layer("tacz:ak47/ak47_shoot", .22, .88), layer("tacz:fn_fal/fn_fal_shoot", .12, .98)),
                    layers(layer("tacz:ak47/ak47_shoot_3p", .30, .88), layer("tacz:fn_fal/fn_fal_shoot_3p", .15, .98)),
                    layers(layer("tacz:ak47/ak47_silence", .19, .90)),
                    layers(layer("tacz:ak47/ak47_silence_3p", .25, .90)));
            case "mini14" -> profile(
                    layers(layer("tacz:m416/m416_shoot", .18, 1.07), layer("tacz:sks/sks_shoot", .12, 1.05)),
                    layers(layer("tacz:m416/m416_shoot_3p", .24, 1.05), layer("tacz:sks/sks_shoot_3p", .15, 1.04)),
                    layers(layer("tacz:m416/m416_silence", .18, 1.06)),
                    layers(layer("tacz:m416/m416_silence_3p", .22, 1.05)));
            case "mk12" -> profile(
                    layers(layer("tacz:sks/sks_shoot", .20, 1.04), layer("tacz:m416/m416_shoot", .10, .95)),
                    layers(layer("tacz:sks/sks_shoot_3p", .27, 1.03), layer("tacz:m416/m416_shoot_3p", .12, .95)),
                    layers(layer("tacz:sks/sks_silence", .20, 1.03)),
                    layers(layer("tacz:sks/sks_silence_3p", .25, 1.03)));
            case "vss" -> profile(
                    layers(layer("tacz:m416/m416_silence", .25, .82), layer("tacz:sks/sks_silence", .13, .90)),
                    layers(layer("tacz:m416/m416_silence_3p", .30, .84), layer("tacz:sks/sks_silence_3p", .16, .90)),
                    layers(layer("tacz:m416/m416_silence", .22, .80), layer("tacz:sks/sks_silence", .10, .88)),
                    layers(layer("tacz:m416/m416_silence_3p", .27, .82), layer("tacz:sks/sks_silence_3p", .12, .88)));
            case "dragunov" -> profile(
                    layers(layer("tacz:sks/sks_shoot", .24, .86), layer("tacz:m700/m700_shoot", .12, 1.02)),
                    layers(layer("tacz:sks/sks_shoot_3p", .31, .86), layer("tacz:m700/m700_shoot_3p", .15, 1.00)),
                    layers(layer("tacz:sks/sks_silence", .21, .88)),
                    layers(layer("tacz:sks/sks_silence_3p", .27, .88)));
            case "tommy_gun" -> profile(
                    layers(layer("tacz:ak47/ak47_shoot", .11, .78), layer("tacz:p320/p320_shoot", .20, .90)),
                    layers(layer("tacz:ak47/ak47_shoot_3p", .15, .80), layer("tacz:p320/p320_shoot_3p", .25, .90)),
                    layers(layer("tacz:p320/p320_silence", .20, .90)),
                    layers(layer("tacz:p320/p320_silence_3p", .25, .90)));
            case "mp9" -> profile(
                    layers(layer("tacz:b93r/b93r_shoot", .24, 1.08), layer("tacz:p90/p90_shoot", .11, 1.12)),
                    layers(layer("tacz:b93r/b93r_shoot_3p", .30, 1.07), layer("tacz:p90/p90_shoot_3p", .14, 1.10)),
                    layers(layer("tacz:b93r/b93r_silence", .21, 1.08)),
                    layers(layer("tacz:b93r/b93r_silence_3p", .25, 1.07)));
            case "js9" -> profile(
                    layers(layer("tacz:mp5k/mp5k_shoot", .20, .96), layer("tacz:m416/m416_shoot", .10, 1.05)),
                    layers(layer("tacz:mp5k/mp5k_shoot_3p", .26, .96), layer("tacz:m416/m416_shoot_3p", .12, 1.04)),
                    layers(layer("tacz:mp5k/mp5k_silence", .19, .97)),
                    layers(layer("tacz:mp5k/mp5k_silence_3p", .23, .97)));
            case "win94" -> profile(
                    layers(layer("tacz:m700/m700_shoot", .25, .88), layer("tacz:deagle/deagle_shoot", .10, .78)),
                    layers(layer("tacz:m700/m700_shoot_3p", .32, .88), layer("tacz:deagle/deagle_shoot_3p", .13, .80)),
                    layers(layer("tacz:m700/m700_silence", .20, .88)),
                    layers(layer("tacz:m700/m700_silence_3p", .25, .88)));
            case "m24" -> profile(
                    layers(layer("tacz:ai_awp/awp_shoot", .24, .93), layer("tacz:m95/m95_shoot", .08, 1.05)),
                    layers(layer("tacz:ai_awp/awp_shoot_3p", .31, .92), layer("tacz:m95/m95_shoot_3p", .10, 1.04)),
                    layers(layer("tacz:ai_awp/awp_silence", .22, .94)),
                    layers(layer("tacz:ai_awp/awp_silence_3p", .27, .93)));
            case "s12k" -> profile(
                    layers(layer("tacz:aa12/aa12_shoot", .24, 1.00), layer("tacz:m870/m870_shoot", .16, 1.05)),
                    layers(layer("tacz:aa12/aa12_shoot_3p", .30, .99), layer("tacz:m870/m870_shoot_3p", .20, 1.03)),
                    layers(layer("tacz:aa12/aa12_silence", .22, 1.00)),
                    layers(layer("tacz:aa12/aa12_silence_3p", .27, 1.00)));
            case "dbs" -> profile(
                    layers(layer("tacz:spas_12/spas12_shoot", .30, .92), layer("tacz:m1014/m1014_shoot", .18, .88)),
                    layers(layer("tacz:m870/m870_shoot_3p", .34, .91), layer("tacz:m1014/m1014_shoot_3p", .20, .88)),
                    layers(layer("tacz:m870/m870_silence", .24, .90)),
                    layers(layer("tacz:m870/m870_silence_3p", .29, .90)));
            case "o12" -> profile(
                    layers(layer("tacz:aa12/aa12_shoot", .28, .94), layer("tacz:m1014/m1014_shoot", .12, 1.05)),
                    layers(layer("tacz:aa12/aa12_shoot_3p", .34, .94), layer("tacz:m1014/m1014_shoot_3p", .15, 1.04)),
                    layers(layer("tacz:aa12/aa12_silence", .22, .96)),
                    layers(layer("tacz:aa12/aa12_silence_3p", .27, .96)));
            case "mg3" -> profile(
                    layers(layer("tacz:m107/m107_shoot", .13, .82), layer("tacz:rpk/rpk_shoot", .22, .88)),
                    layers(layer("tacz:m107/m107_shoot_3p", .17, .82), layer("tacz:rpk/rpk_shoot_3p", .29, .88)),
                    layers(layer("tacz:rpk/rpk_silence", .22, .88)),
                    layers(layer("tacz:rpk/rpk_silence_3p", .27, .88)));
            case "rpd" -> profile(
                    layers(layer("tacz:m249/m249_shoot", .20, .90), layer("tacz:ak47/ak47_shoot", .14, .86)),
                    layers(layer("tacz:m249/m249_shoot_3p", .26, .90), layer("tacz:ak47/ak47_shoot_3p", .18, .86)),
                    layers(layer("tacz:rpk/rpk_silence", .21, .90)),
                    layers(layer("tacz:rpk/rpk_silence_3p", .26, .90)));
            case "skorpion" -> profile(
                    layers(layer("tacz:mp5k/mp5k_shoot", .22, 1.10), layer("tacz:p90/p90_shoot", .10, 1.08)),
                    layers(layer("tacz:mp5k/mp5k_shoot_3p", .28, 1.08), layer("tacz:p90/p90_shoot_3p", .13, 1.08)),
                    layers(layer("tacz:mp5k/mp5k_silence", .20, 1.08)),
                    layers(layer("tacz:mp5k/mp5k_silence_3p", .24, 1.08)));
            case "r1895" -> profile(
                    layers(layer("tacz:deagle/deagle_shoot", .24, .78), layer("tacz:m700/m700_shoot", .08, 1.05)),
                    layers(layer("tacz:deagle/deagle_shoot_3p", .31, .78), layer("tacz:m700/m700_shoot_3p", .10, 1.04)),
                    layers(layer("tacz:deagle/deagle_silence", .20, .82)),
                    layers(layer("tacz:deagle/deagle_silence_3p", .25, .82)));
            default -> throw new IllegalArgumentException("Missing sound profile for " + id);
        };
    }

    private static SoundProfile profile(List<SoundLayerSpec> shoot, List<SoundLayerSpec> shoot3p,
                                        List<SoundLayerSpec> silence, List<SoundLayerSpec> silence3p) {
        return new SoundProfile(shoot, shoot3p, silence, silence3p);
    }

    private static List<SoundLayerSpec> layers(SoundLayerSpec... layers) {
        return List.of(layers);
    }

    private static SoundLayerSpec layer(String sound, double volume, double pitch) {
        return new SoundLayerSpec(sound, volume, pitch);
    }

    private static Model createModel(Spec s) {
        List<Box> body = new ArrayList<>();
        List<Box> mag = new ArrayList<>();
        List<Box> bolt = new ArrayList<>();
        String profile = s.s("profile");

        switch (profile) {
            case "bullpup_ak" -> bullpup(body, mag, s, true);
            case "bullpup_ar" -> bullpup(body, mag, s, false);
            case "ak" -> standardRifle(body, mag, s, true, false);
            case "ar" -> standardRifle(body, mag, s, false, false);
            case "dmr" -> standardRifle(body, mag, s, false, true);
            case "dmr_wood" -> standardRifle(body, mag, s, true, true);
            case "vss" -> vss(body, mag, s);
            case "smg_wood" -> tommy(body, mag, s);
            case "smg_compact" -> compactSmg(body, mag, s);
            case "bullpup_smg" -> bullpupSmg(body, mag, s);
            case "lever" -> lever(body, mag, s);
            case "sniper" -> sniper(body, mag, s);
            case "shotgun_mag" -> shotgunMag(body, mag, s);
            case "shotgun_bullpup" -> shotgunBullpup(body, mag, s);
            case "shotgun_drum" -> shotgunDrum(body, mag, s);
            case "lmg_belt" -> lmg(body, mag, s, false);
            case "lmg_rpd" -> lmg(body, mag, s, true);
            case "machine_pistol" -> machinePistol(body, mag, s);
            case "revolver" -> revolver(body, mag, s);
            default -> throw new IllegalArgumentException("Unknown profile: " + profile);
        }

        double bodyLen = s.d("body");
        bolt.add(new Box(-.75, 9.25, -.5, 1.5, .55, Math.max(2.2, bodyLen * .32), "accent"));
        return new Model(body, mag, bolt);
    }

    private static void standardRifle(List<Box> b, List<Box> m, Spec s, boolean wood, boolean dmr) {
        double body = s.d("body"), barrel = s.d("barrel"), stock = s.d("stock");
        b.add(new Box(-1.15, 7.1, -body / 2, 2.3, 2.7, body, "metal"));
        b.add(new Box(-1.0, 9.65, -body / 2 + .6, 2.0, .55, body - 1.2, "accent"));
        b.add(new Box(-.42, 8.0, -body / 2 - barrel, .84, .84, barrel, "dark"));
        b.add(new Box(-1.0, 7.45, -body / 2 - barrel * .62, 2.0, 2.0, barrel * .62, "poly"));
        b.add(new Box(-.82, 3.7, body / 2 - 3.1, 1.64, 3.8, 2.0, "poly"));
        b.add(new Box(-1.0, 7.0, body / 2, 2.0, 2.6, stock * .45, wood ? "wood" : "poly"));
        b.add(new Box(-1.25, 6.5, body / 2 + stock * .38, 2.5, 3.6, stock * .62, wood ? "wood" : "poly"));
        rail(b, -body / 2 + .5, body - 1.0);
        sightPair(b, -body / 2 - barrel + 1.0, body / 2 - 1.0);
        if (dmr) {
            b.add(new Box(-.7, 10.2, -1.0, 1.4, .35, 6.0, "accent"));
            b.add(new Box(-.5, 9.95, -body / 2 - barrel * .85, 1.0, .45, barrel * .20, "dark"));
        }
        magazine(m, body / 2 - 4.0, 4.2, wood ? "accent" : "dark", false);
    }

    private static void bullpup(List<Box> b, List<Box> m, Spec s, boolean akStyle) {
        double body = s.d("body"), barrel = s.d("barrel");
        b.add(new Box(-1.25, 6.8, -body / 2, 2.5, 3.4, body + 4.0, "poly"));
        b.add(new Box(-1.0, 8.8, -body / 2 - 1.2, 2.0, 1.5, body + 1.5, "metal"));
        b.add(new Box(-.42, 8.1, -body / 2 - barrel, .84, .84, barrel, "dark"));
        b.add(new Box(-1.0, 7.4, -body / 2 - barrel * .58, 2.0, 1.9, barrel * .58, "poly"));
        b.add(new Box(-.85, 3.5, -1.0, 1.7, 3.8, 1.9, "poly"));
        b.add(new Box(-1.35, 6.1, body / 2 + 2.0, 2.7, 4.3, 2.4, "poly"));
        rail(b, -body / 2, body - .5);
        sightPair(b, -body / 2 - barrel + .8, body / 2 + 1.0);
        magazine(m, body / 2 - 1.0, 3.8, akStyle ? "accent" : "dark", akStyle);
    }

    private static void vss(List<Box> b, List<Box> m, Spec s) {
        double body = s.d("body"), barrel = s.d("barrel"), stock = s.d("stock");
        b.add(new Box(-1.0, 7.1, -body / 2, 2.0, 2.6, body, "metal"));
        b.add(new Box(-.95, 7.4, -body / 2 - barrel, 1.9, 1.75, barrel, "dark"));
        b.add(new Box(-1.15, 6.8, -body / 2 - barrel * .72, 2.3, 2.5, barrel * .72, "dark"));
        b.add(new Box(-.8, 3.8, body / 2 - 2.7, 1.6, 3.4, 1.9, "wood"));
        b.add(new Box(-.55, 7.4, body / 2, 1.1, 1.0, stock, "wood"));
        b.add(new Box(-1.15, 5.8, body / 2 + stock * .55, 2.3, 2.3, stock * .45, "wood"));
        b.add(new Box(-.9, 10.0, -.5, 1.8, 1.0, 5.5, "accent"));
        b.add(new Box(-1.15, 9.75, .2, 2.3, 1.5, 3.1, "dark"));
        magazine(m, body / 2 - 3.8, 3.8, "dark", true);
    }

    private static void tommy(List<Box> b, List<Box> m, Spec s) {
        double body = s.d("body"), barrel = s.d("barrel"), stock = s.d("stock");
        b.add(new Box(-1.1, 7.0, -body / 2, 2.2, 2.8, body, "metal"));
        b.add(new Box(-.35, 8.0, -body / 2 - barrel, .7, .7, barrel, "dark"));
        b.add(new Box(-.75, 6.8, -body / 2 - barrel * .55, 1.5, 1.6, barrel * .55, "wood"));
        b.add(new Box(-.75, 3.6, -body / 2 + 1.0, 1.5, 3.5, 1.8, "wood"));
        b.add(new Box(-1.2, 6.0, body / 2, 2.4, 3.4, stock, "wood"));
        magazine(m, -1.5, 4.5, "dark", false);
        sightPair(b, -body / 2 - barrel + 1.0, body / 2 - 1.0);
    }

    private static void compactSmg(List<Box> b, List<Box> m, Spec s) {
        double body = s.d("body"), barrel = s.d("barrel"), stock = s.d("stock");
        b.add(new Box(-1.0, 7.0, -body / 2, 2.0, 2.8, body, "metal"));
        b.add(new Box(-.34, 8.0, -body / 2 - barrel, .68, .68, barrel, "dark"));
        b.add(new Box(-.8, 3.7, body / 2 - 2.2, 1.6, 3.4, 1.7, "poly"));
        b.add(new Box(-.22, 7.5, body / 2, .44, .44, stock, "accent"));
        b.add(new Box(-1.0, 6.8, body / 2 + stock - .6, 2.0, 1.5, .6, "poly"));
        rail(b, -body / 2 + .3, body - .6);
        magazine(m, 1.1, 5.2, "dark", false);
    }

    private static void bullpupSmg(List<Box> b, List<Box> m, Spec s) {
        double body = s.d("body"), barrel = s.d("barrel");
        b.add(new Box(-1.3, 6.6, -body / 2, 2.6, 3.8, body + 3.0, "poly"));
        b.add(new Box(-.9, 8.5, -body / 2 - .8, 1.8, 1.5, body, "metal"));
        b.add(new Box(-.34, 8.0, -body / 2 - barrel, .68, .68, barrel, "dark"));
        b.add(new Box(-.75, 3.7, -.6, 1.5, 3.2, 1.6, "poly"));
        b.add(new Box(-1.15, 9.95, -2.0, 2.3, .42, 8.0, "accent"));
        magazine(m, body / 2, 4.6, "dark", false);
    }

    private static void lever(List<Box> b, List<Box> m, Spec s) {
        double body = s.d("body"), barrel = s.d("barrel"), stock = s.d("stock");
        b.add(new Box(-.85, 7.0, -body / 2, 1.7, 2.0, body, "metal"));
        b.add(new Box(-.28, 8.0, -body / 2 - barrel, .56, .56, barrel, "dark"));
        b.add(new Box(-.5, 6.9, -body / 2 - barrel * .55, 1.0, 1.4, barrel * .55, "wood"));
        b.add(new Box(-1.05, 5.9, body / 2, 2.1, 3.2, stock, "wood"));
        b.add(new Box(-.75, 3.7, body / 2 - 2.6, 1.5, 3.0, 1.8, "wood"));
        b.add(new Box(-.18, 4.7, 1.0, .36, .36, 4.2, "accent"));
        b.add(new Box(-.18, 4.2, 1.0, .36, 1.0, .36, "accent"));
        b.add(new Box(-.18, 4.2, 4.8, .36, 1.0, .36, "accent"));
        sightPair(b, -body / 2 - barrel + 1.2, body / 2 - .8);
    }

    private static void sniper(List<Box> b, List<Box> m, Spec s) {
        double body = s.d("body"), barrel = s.d("barrel"), stock = s.d("stock");
        b.add(new Box(-1.0, 7.0, -body / 2, 2.0, 2.5, body, "metal"));
        b.add(new Box(-.3, 8.0, -body / 2 - barrel, .6, .6, barrel, "dark"));
        b.add(new Box(-.85, 6.3, -body / 2 - barrel * .5, 1.7, 1.7, barrel * .5, "poly"));
        b.add(new Box(-.95, 5.9, body / 2, 1.9, 3.4, stock, "poly"));
        b.add(new Box(-.8, 3.7, body / 2 - 2.6, 1.6, 3.0, 1.8, "poly"));
        b.add(new Box(-.9, 9.8, -1.0, 1.8, .4, 5.5, "accent"));
        magazine(m, 0.6, 3.5, "dark", false);
    }

    private static void shotgunMag(List<Box> b, List<Box> m, Spec s) {
        double body = s.d("body"), barrel = s.d("barrel"), stock = s.d("stock");
        b.add(new Box(-1.1, 7.0, -body / 2, 2.2, 2.8, body, "metal"));
        b.add(new Box(-.38, 8.2, -body / 2 - barrel, .76, .76, barrel, "dark"));
        b.add(new Box(-.42, 6.9, -body / 2 - barrel, .84, .55, barrel * .88, "dark"));
        b.add(new Box(-1.0, 6.7, -body / 2 - barrel * .5, 2.0, 2.0, barrel * .5, "poly"));
        b.add(new Box(-.9, 5.7, body / 2, 1.8, 3.3, stock, "poly"));
        b.add(new Box(-.8, 3.5, body / 2 - 2.8, 1.6, 3.5, 1.8, "poly"));
        magazine(m, .2, 4.3, "dark", false);
        rail(b, -body / 2 + .5, body - 1.0);
    }

    private static void shotgunBullpup(List<Box> b, List<Box> m, Spec s) {
        double body = s.d("body"), barrel = s.d("barrel");
        b.add(new Box(-1.3, 6.7, -body / 2, 2.6, 3.5, body + 3.0, "poly"));
        b.add(new Box(-1.0, 8.4, -body / 2 - .7, 2.0, 1.5, body, "metal"));
        b.add(new Box(-.72, 8.15, -body / 2 - barrel, .58, .58, barrel, "dark"));
        b.add(new Box(.14, 8.15, -body / 2 - barrel, .58, .58, barrel, "dark"));
        b.add(new Box(-.62, 6.8, -body / 2 - barrel, .48, .48, barrel * .9, "accent"));
        b.add(new Box(.14, 6.8, -body / 2 - barrel, .48, .48, barrel * .9, "accent"));
        b.add(new Box(-.8, 3.5, -.8, 1.6, 3.4, 1.8, "poly"));
        b.add(new Box(-1.2, 9.9, -2.0, 2.4, .4, 8.5, "accent"));
    }

    private static void shotgunDrum(List<Box> b, List<Box> m, Spec s) {
        double body = s.d("body"), barrel = s.d("barrel"), stock = s.d("stock");
        b.add(new Box(-1.2, 7.0, -body / 2, 2.4, 3.0, body, "metal"));
        b.add(new Box(-.4, 8.2, -body / 2 - barrel, .8, .8, barrel, "dark"));
        b.add(new Box(-1.0, 6.9, -body / 2 - barrel * .55, 2.0, 2.1, barrel * .55, "poly"));
        b.add(new Box(-.9, 5.8, body / 2, 1.8, 3.2, stock, "poly"));
        b.add(new Box(-.8, 3.6, body / 2 - 2.4, 1.6, 3.5, 1.8, "poly"));
        b.add(new Box(-2.0, 2.4, .4, 4.0, 4.0, 2.0, "dark"));
        b.add(new Box(-.8, 3.1, -3.2, 1.6, 3.2, 1.6, "poly"));
        rail(b, -body / 2 + .3, body - .6);
    }

    private static void lmg(List<Box> b, List<Box> m, Spec s, boolean wood) {
        double body = s.d("body"), barrel = s.d("barrel"), stock = s.d("stock");
        b.add(new Box(-1.35, 6.8, -body / 2, 2.7, 3.4, body, "metal"));
        b.add(new Box(-.45, 8.2, -body / 2 - barrel, .9, .9, barrel, "dark"));
        b.add(new Box(-1.1, 7.0, -body / 2 - barrel * .58, 2.2, 2.3, barrel * .58, "metal"));
        b.add(new Box(-1.05, 5.8, body / 2, 2.1, 3.5, stock, wood ? "wood" : "poly"));
        b.add(new Box(-.8, 3.5, body / 2 - 2.8, 1.6, 3.5, 1.8, wood ? "wood" : "poly"));
        b.add(new Box(-1.8, 2.7, -.5, 3.6, 4.0, 3.0, "dark"));
        b.add(new Box(-.16, 2.2, -body / 2 - barrel * .48, .32, 4.8, .32, "accent"));
        b.add(new Box(-.16, 2.2, -body / 2 - barrel * .74, .32, 4.8, .32, "accent"));
        b.add(new Box(-.8, 10.2, -1.0, 1.6, .35, 6.0, "accent"));
        if (wood) {
            b.add(new Box(-.75, 6.5, -body / 2 - barrel * .38, 1.5, 1.5, barrel * .32, "wood"));
        }
        magazine(m, -.2, 3.8, "dark", false);
    }

    private static void machinePistol(List<Box> b, List<Box> m, Spec s) {
        double body = s.d("body"), barrel = s.d("barrel"), stock = s.d("stock");
        b.add(new Box(-.8, 7.0, -body / 2, 1.6, 2.4, body, "metal"));
        b.add(new Box(-.25, 7.8, -body / 2 - barrel, .5, .5, barrel, "dark"));
        b.add(new Box(-.7, 3.8, body / 2 - 2.0, 1.4, 3.5, 1.5, "poly"));
        b.add(new Box(-.18, 7.3, body / 2, .36, .36, stock, "accent"));
        b.add(new Box(-.8, 6.6, body / 2 + stock - .5, 1.6, 1.3, .5, "poly"));
        rail(b, -body / 2 + .3, body - .5);
        magazine(m, .8, 5.5, "dark", false);
    }

    private static void revolver(List<Box> b, List<Box> m, Spec s) {
        double body = s.d("body"), barrel = s.d("barrel");
        b.add(new Box(-.85, 7.0, -body / 2, 1.7, 2.2, body, "metal"));
        b.add(new Box(-.3, 7.9, -body / 2 - barrel, .6, .6, barrel, "dark"));
        b.add(new Box(-1.25, 6.7, -.5, 2.5, 2.5, 2.4, "accent"));
        b.add(new Box(-.75, 3.5, body / 2 - 2.2, 1.5, 3.7, 1.8, "wood"));
        b.add(new Box(-.45, 9.25, -body / 2 - barrel + .8, .9, .35, .6, "accent"));
        b.add(new Box(-.45, 9.25, body / 2 - .8, .9, .35, .6, "accent"));
    }

    private static void magazine(List<Box> m, double z, double height, String material, boolean curved) {
        m.add(new Box(-.78, 6.7 - height, z, 1.56, height, 2.0, material));
        if (curved) {
            m.add(new Box(-.72, 5.0 - height, z + 1.5, 1.44, height * .55, 1.6, material));
        }
    }

    private static void rail(List<Box> b, double z, double length) {
        b.add(new Box(-.7, 10.0, z, 1.4, .3, length, "accent"));
        int teeth = Math.max(4, (int) (length / 1.2));
        for (int i = 0; i < teeth; i++) {
            double p = z + i * (length / teeth);
            b.add(new Box(-.8, 10.28, p, 1.6, .18, .35, "dark"));
        }
    }

    private static void sightPair(List<Box> b, double frontZ, double rearZ) {
        b.add(new Box(-.12, 9.4, frontZ, .24, .9, .35, "dark"));
        b.add(new Box(-.35, 9.4, rearZ, .7, .75, .45, "dark"));
    }

    private static String magAnimationWrapper(Spec s) {
        return switch (s.s("base")) {
            case "ak47", "rpk" -> "lefthand_and_mag";
            case "scar_h" -> "mag_and_lh";
            case "ump45" -> "magzine_and_bullet";
            case "p90" -> "p90_mag_standard";
            default -> null;
        };
    }

    private static String gunAnimationWrapper(Spec s) {
        return switch (s.s("base")) {
            case "scar_h", "springfield1873" -> "gun_and_rh";
            case "ump45" -> "ump45";
            case "mp5k" -> "Mp5k";
            case "rpk", "rhino357" -> "righthand_and_gun";
            default -> null;
        };
    }

    private static String boltAnimationWrapper(Spec s) {
        return switch (s.s("base")) {
            case "aug" -> "aug_bolt";
            case "m16a1" -> "m4a1_bolt";
            case "ump45", "p90" -> "ump45_bolt";
            case "m1014" -> "Bolt";
            case "m870" -> "slide2";
            default -> null;
        };
    }

    private static String additionalMagazineBone(Spec s) {
        return switch (s.s("base")) {
            case "m16a1" -> "additional_magzine";
            case "ak47", "scar_h", "sks_tactical", "spr15hb", "fn_fal", "mp5k" -> "additional_magazine";
            default -> null;
        };
    }

    private static String geometryJson(Spec s, Model model, boolean lod) {
        List<Box> body = lod ? simplify(model.body()) : model.body();
        List<Box> mag = lod ? simplify(model.magazine()) : model.magazine();
        List<Box> bolt = lod ? simplify(model.bolt()) : model.bolt();

        double muzzleZ = body.stream().mapToDouble(Box::z).min().orElse(-15);
        double rearZ = body.stream().mapToDouble(box -> box.z() + box.d()).max().orElse(15);

        StringBuilder out = new StringBuilder();
        out.append("{\n  \"format_version\": \"1.12.0\",\n  \"minecraft:geometry\": [\n    {\n");
        out.append("      \"description\": {\n");
        out.append("        \"identifier\": \"geometry.tacz.").append(s.s("id")).append(lod ? ".lod" : "").append("\",\n");
        out.append("        \"texture_width\": 256, \"texture_height\": 256,\n");
        out.append("        \"visible_bounds_width\": 7, \"visible_bounds_height\": 3.5, \"visible_bounds_offset\": [0, 0.7, 0]\n");
        out.append("      },\n      \"bones\": [\n");
        String magWrapper = magAnimationWrapper(s);
        String gunWrapper = gunAnimationWrapper(s);
        String boltWrapper = boltAnimationWrapper(s);

        bone(out, "root", null, new double[]{0, 7.5, 6}, List.of(), true);
        if (magWrapper != null) {
            bone(out, magWrapper, "root", new double[]{0, 5, 1}, List.of(), true);
        }
        bone(out, "mag_and_lefthand", magWrapper != null ? magWrapper : "root", new double[]{0, 5, 1}, List.of(), true);
        bone(out, "lefthand", "mag_and_lefthand", new double[]{-6, 19, 0}, List.of(), true);
        bone(out, "lefthand_pos", "lefthand", new double[]{0, 8, 0}, List.of(), true);
        bone(out, "mag_and_bullet", "mag_and_lefthand", new double[]{0, 5, 1}, List.of(), true);
        bone(out, "bullet", "mag_and_bullet", new double[]{0, 8, 0}, List.of(new Box(-.12, 8.0, -.2, .24, .24, 1.1, "accent")), true);
        bone(out, "magazine", "mag_and_bullet", new double[]{0, 5, 1}, List.of(), true);
        bone(out, "bullet_in_mag", "magazine", new double[]{0, 8, 0}, List.of(new Box(-.12, 7.8, .2, .24, .24, 1.0, "accent")), true);
        bone(out, "mag_standard", "magazine", new double[]{0, 5, 1}, mag, true);
        bone(out, "mag_extended_1", "magazine", new double[]{0, 5, 1}, scaledMagazine(mag, 1.15), true);
        bone(out, "mag_extended_2", "magazine", new double[]{0, 5, 1}, scaledMagazine(mag, 1.30), true);
        bone(out, "mag_extended_3", "magazine", new double[]{0, 5, 1}, scaledMagazine(mag, 1.45), true);
        String additionalMagazine = additionalMagazineBone(s);
        if (additionalMagazine != null) {
            bone(out, additionalMagazine, "root", new double[]{0, 5, 1}, mag, true);
        }
        if (gunWrapper != null) {
            bone(out, gunWrapper, "root", new double[]{0, 7.5, 0}, List.of(), true);
        }
        bone(out, "gun_and_righthand", gunWrapper != null ? gunWrapper : "root", new double[]{0, 7.5, 0}, List.of(), true);
        bone(out, "righthand", "gun_and_righthand", new double[]{6, 19, 0}, List.of(), true);
        bone(out, "righthand_pos", "righthand", new double[]{0, 8, 0}, List.of(), true);
        bone(out, "default_gun", "gun_and_righthand", new double[]{0, 0, 0}, body, true);
        if (boltWrapper != null) {
            bone(out, boltWrapper, "default_gun", new double[]{0, 8, 0}, List.of(), true);
        }
        bone(out, "bullet_and_bolt", boltWrapper != null ? boltWrapper : "default_gun", new double[]{0, 8, 0}, List.of(), true);
        bone(out, "bolt", "bullet_and_bolt", new double[]{0, 8, 0}, bolt, true);
        bone(out, "bullet_in_barrel", "bolt", new double[]{0, 8, -1}, List.of(new Box(-.10, 7.9, muzzleZ + 2, .20, .20, .8, "accent")), true);
        bone(out, "charge_handle", "default_gun", new double[]{0, 8, 0}, List.of(), true);
        bone(out, "slide", "default_gun", new double[]{0, 8, 0}, List.of(), true);
        bone(out, "positioning2", "default_gun", new double[]{0, 0, 0}, List.of(), true);
        bone(out, "muzzle_flash", "positioning2", new double[]{0, 8.5, muzzleZ - .7}, List.of(), true);
        bone(out, "muzzle_pos", "positioning2", new double[]{0, 8.5, muzzleZ}, List.of(), true);
        bone(out, "scope_pos", "positioning2", new double[]{0, 10.5, 1.5}, List.of(), true);
        bone(out, "stock_pos", "positioning2", new double[]{0, 8.0, rearZ}, List.of(), true);
        bone(out, "grip_pos", "positioning2", new double[]{0, 5.2, -3.0}, List.of(), true);
        bone(out, "laser_pos", "positioning2", new double[]{1.2, 8.5, -5.0}, List.of(), true);
        bone(out, "shell", "positioning2", new double[]{-1.1, 8.6, 1.0}, List.of(), true);
        bone(out, "attachment_adapter", "default_gun", new double[]{0, 0, 0}, List.of(), true);
        bone(out, "camera", null, new double[]{2.7, 12.5, 14}, List.of(), true);
        bone(out, "thirdperson_hand", "root", new double[]{0, 6.5, 6}, List.of(), true);
        bone(out, "refit_muzzle_view", "root", new double[]{10, 9, muzzleZ - 4}, List.of(), true);
        bone(out, "refit_stock_view", "root", new double[]{10, 9, rearZ + 2}, List.of(), true);
        bone(out, "refit_scope_view", "root", new double[]{8, 13, 4}, List.of(), true);
        bone(out, "refit_extended_mag_view", "root", new double[]{8, 1, 1}, List.of(), true);
        bone(out, "refit_laser_view", "root", new double[]{8, 10, -6}, List.of(), true);
        bone(out, "refit_grip_view", "root", new double[]{7, 5, -5}, List.of(), true);
        out.setLength(out.length() - 2);
        out.append("\n      ]\n    }\n  ]\n}\n");
        return out.toString();
    }

    private static void bone(StringBuilder out, String name, String parent, double[] pivot, List<Box> cubes, boolean comma) {
        out.append("        {\n          \"name\": \"").append(name).append("\",");
        if (parent != null) out.append("\n          \"parent\": \"").append(parent).append("\",");
        out.append("\n          \"pivot\": [").append(n(pivot[0])).append(", ").append(n(pivot[1])).append(", ").append(n(pivot[2])).append("]");
        if (!cubes.isEmpty()) {
            out.append(",\n          \"cubes\": [\n");
            for (int i = 0; i < cubes.size(); i++) {
                if (i > 0) out.append(",\n");
                out.append(cubeJson(cubes.get(i), "            "));
            }
            out.append("\n          ]");
        }
        out.append("\n        }");
        if (comma) out.append(",\n");
        else out.append("\n");
    }

    private static String cubeJson(Box b, String indent) {
        int[] uv = uv(b.material());
        String face = "{\"uv\":[" + uv[0] + "," + uv[1] + "],\"uv_size\":[32,32]}";
        return indent + "{\"origin\":[" + n(b.x()) + "," + n(b.y()) + "," + n(b.z()) + "],"
                + "\"size\":[" + n(b.w()) + "," + n(b.h()) + "," + n(b.d()) + "],"
                + "\"uv\":{\"north\":" + face + ",\"east\":" + face + ",\"south\":" + face
                + ",\"west\":" + face + ",\"up\":" + face + ",\"down\":" + face + "}}";
    }

    private static List<Box> simplify(List<Box> boxes) {
        List<Box> result = new ArrayList<>();
        for (Box box : boxes) {
            double volume = box.w() * box.h() * box.d();
            if (volume >= .6 || result.size() < 14) result.add(box);
            if (result.size() >= 28) break;
        }
        return result;
    }

    private static List<Box> scaledMagazine(List<Box> boxes, double scale) {
        List<Box> result = new ArrayList<>();
        for (Box b : boxes) {
            double extra = b.h() * (scale - 1.0);
            result.add(new Box(b.x(), b.y() - extra, b.z(), b.w(), b.h() * scale, b.d(), b.material()));
        }
        return result;
    }

    private static void renderTexture(Spec s, Path target, TextureKind kind) throws IOException {
        BufferedImage image = new BufferedImage(TEX_SIZE, TEX_SIZE, BufferedImage.TYPE_INT_ARGB);
        Color metal = color(s.s("metal"));
        Color poly = color(s.s("poly"));
        Color accent = color(s.s("accent"));
        Color wood = color(s.s("wood"));

        if (kind == TextureKind.NORMAL) {
            for (int y = 0; y < TEX_SIZE; y++) {
                for (int x = 0; x < TEX_SIZE; x++) {
                    int nx = 126 + ((x * 17 + y * 7 + s.s("id").hashCode()) & 3);
                    int ny = 126 + ((x * 5 + y * 13 + s.s("id").hashCode()) & 3);
                    image.setRGB(x, y, new Color(nx, ny, 255, 255).getRGB());
                }
            }
        } else if (kind == TextureKind.SPECULAR) {
            fillTile(image, 0, 0, 64, 64, new Color(210, 210, 210), s.s("id").hashCode());
            fillTile(image, 64, 0, 64, 64, new Color(235, 235, 235), s.s("id").hashCode() + 1);
            fillTile(image, 128, 0, 64, 64, new Color(65, 65, 65), s.s("id").hashCode() + 2);
            fillTile(image, 192, 0, 64, 64, new Color(45, 45, 45), s.s("id").hashCode() + 3);
            fillTile(image, 0, 64, 64, 64, new Color(160, 160, 160), s.s("id").hashCode() + 4);
            fillTile(image, 64, 64, 64, 64, new Color(100, 100, 100), s.s("id").hashCode() + 5);
        } else {
            fillTile(image, 0, 0, 64, 64, metal, s.s("id").hashCode());
            fillTile(image, 64, 0, 64, 64, darken(metal, .72), s.s("id").hashCode() + 1);
            fillTile(image, 128, 0, 64, 64, poly, s.s("id").hashCode() + 2);
            fillTile(image, 192, 0, 64, 64, wood, s.s("id").hashCode() + 3);
            fillTile(image, 0, 64, 64, 64, accent, s.s("id").hashCode() + 4);
            fillTile(image, 64, 64, 64, 64, lighten(accent, 1.25), s.s("id").hashCode() + 5);
            Graphics2D g = image.createGraphics();
            g.setColor(new Color(255,255,255,35));
            for (int y = 4; y < 128; y += 11) g.drawLine(0, y, 255, y + 3);
            g.dispose();
        }

        Files.createDirectories(target.getParent());
        ImageIO.write(image, "png", target.toFile());
    }

    private static void fillTile(BufferedImage image, int ox, int oy, int w, int h, Color base, int seed) {
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int noise = ((x * 31 + y * 17 + seed) ^ (x * y + seed * 13)) & 15;
                double factor = .91 + noise / 120.0;
                int r = clamp((int) (base.getRed() * factor));
                int g = clamp((int) (base.getGreen() * factor));
                int b = clamp((int) (base.getBlue() * factor));
                image.setRGB(ox + x, oy + y, new Color(r, g, b, 255).getRGB());
            }
        }
    }

    private static void renderIcon(Spec s, Model model, Path target, int width, int height, boolean hud) throws IOException {
        List<Box> all = new ArrayList<>();
        all.addAll(model.body());
        all.addAll(model.magazine());
        all.addAll(model.bolt());

        double minZ = all.stream().mapToDouble(Box::z).min().orElse(-10);
        double maxZ = all.stream().mapToDouble(b -> b.z() + b.d()).max().orElse(10);
        double minY = all.stream().mapToDouble(Box::y).min().orElse(0);
        double maxY = all.stream().mapToDouble(b -> b.y() + b.h()).max().orElse(12);

        double sx = (width - 18.0) / Math.max(1, maxZ - minZ);
        double sy = (height - 14.0) / Math.max(1, maxY - minY);
        double scale = Math.min(sx, sy);
        double usedW = (maxZ - minZ) * scale;
        double usedH = (maxY - minY) * scale;
        double offX = (width - usedW) / 2.0;
        double offY = (height - usedH) / 2.0;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(hud ? 1.0f : 1.6f));

        for (Box box : all) {
            int x = (int) Math.round(offX + (maxZ - (box.z() + box.d())) * scale);
            int y = (int) Math.round(offY + (maxY - (box.y() + box.h())) * scale);
            int w = Math.max(1, (int) Math.round(box.d() * scale));
            int h = Math.max(1, (int) Math.round(box.h() * scale));
            Color c = materialColor(s, box.material());
            if (hud) c = lighten(c, 1.18);
            g.setPaint(new GradientPaint(x, y, lighten(c, 1.16), x, y + h, darken(c, .72)));
            g.fillRoundRect(x, y, w, h, Math.min(4, h / 3), Math.min(4, h / 3));
            g.setColor(new Color(10, 12, 14, hud ? 180 : 215));
            g.drawRoundRect(x, y, w, h, Math.min(4, h / 3), Math.min(4, h / 3));
        }

        g.dispose();
        Files.createDirectories(target.getParent());
        ImageIO.write(image, "png", target.toFile());
    }

    private static Color materialColor(Spec s, String material) {
        return switch (material) {
            case "dark" -> darken(color(s.s("metal")), .62);
            case "poly" -> color(s.s("poly"));
            case "wood" -> color(s.s("wood"));
            case "accent" -> color(s.s("accent"));
            default -> color(s.s("metal"));
        };
    }

    private static int[] uv(String material) {
        return switch (material) {
            case "dark" -> new int[]{64, 0};
            case "poly" -> new int[]{128, 0};
            case "wood" -> new int[]{192, 0};
            case "accent" -> new int[]{0, 64};
            default -> new int[]{0, 0};
        };
    }

    private static Color color(String hex) {
        return new Color(Integer.parseInt(hex, 16));
    }

    private static Color darken(Color c, double factor) {
        return new Color(clamp((int)(c.getRed() * factor)), clamp((int)(c.getGreen() * factor)), clamp((int)(c.getBlue() * factor)));
    }

    private static Color lighten(Color c, double factor) {
        return new Color(clamp((int)(c.getRed() * factor)), clamp((int)(c.getGreen() * factor)), clamp((int)(c.getBlue() * factor)));
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static String jsonStringValue(String json, String key) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String replaceJsonStringValue(String json, String key, String value) {
        return json.replaceFirst("(\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*)\\\"[^\\\"]+\\\"", "$1\\\"" + Matcher.quoteReplacement(value) + "\\\"");
    }

    private static List<String> csv(String value) {
        List<String> out = new ArrayList<>();
        if (value == null || value.isBlank()) return out;
        for (String item : value.split(",")) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) out.add(trimmed);
        }
        return out;
    }

    private static String n(double value) {
        String formatted = String.format(Locale.ROOT, "%.4f", value);
        while (formatted.contains(".") && (formatted.endsWith("0") || formatted.endsWith("."))) {
            formatted = formatted.substring(0, formatted.length() - 1);
        }
        return formatted;
    }

    private static void write(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }

    private static void clearDirectory(Path directory) throws IOException {
        if (!Files.exists(directory)) return;
        try (var stream = Files.walk(directory)) {
            stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }
            });
        }
    }

    private enum TextureKind { COLOR, NORMAL, SPECULAR }

    private record Box(double x, double y, double z, double w, double h, double d, String material) {
    }

    private record Model(List<Box> body, List<Box> magazine, List<Box> bolt) {
    }

    private record SoundLayerSpec(String sound, double volume, double pitch) {
    }

    private record SoundProfile(List<SoundLayerSpec> shoot, List<SoundLayerSpec> shoot3p,
                                List<SoundLayerSpec> silence, List<SoundLayerSpec> silence3p) {
    }

    private record Spec(Map<String, String> values) {
        String s(String key) {
            String value = values.get(key);
            if (value == null) throw new IllegalArgumentException("Missing spec key " + key + " for " + values.get("id"));
            return value;
        }

        int i(String key) {
            return (int) Math.round(Double.parseDouble(s(key)));
        }

        double d(String key) {
            return Double.parseDouble(s(key));
        }

        boolean b(String key) {
            return Boolean.parseBoolean(s(key));
        }
    }
}
