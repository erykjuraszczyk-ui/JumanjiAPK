package com.jumanji.worldoffantasy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import java.util.Random;

/** Native, self-contained 2D/2.5D exploration game renderer. */
public final class FantasyGameView extends View {
    private enum Page { TITLE, PLAY, MAP, JOURNAL, BAG, PAUSE }
    private static final float W=5000f,H=3200f,CX=2500f,CY=1600f,SX=2500f,SY=300f;
    private static final float[] RX={800,4200,4200,800}, RY={800,800,2400,2400};
    private static final int[] RC={0xff267c58,0xffb84e35,0xff5b58bb,0xff5a9fbd};
    private static final String[] RN={"ZIELONE KRÓLESTWO","KRAINA OGNIA","KRYSZTAŁOWE ARKANA","LODOWE PUSTKOWIE"};
    private static final String[] QUEST={"Obudź pradawne nasiona","Przywróć płomień kuźni","Odnajdź kryształy pamięci","Zapal latarnie zorzy"};
    private static final float[][] TX={{650,930,810},{4020,4380,4200},{4020,4380,4200},{650,950,800}};
    private static final float[][] TY={{700,720,1000},{700,720,990},{2300,2320,2650},{2320,2300,2650}};
    private static final int GOLD=0xffffd477, INK=0xff0b1c31, WHITE=0xfff4f0e6;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final GameProgress g;
    private final float d;
    private Page page=Page.TITLE;
    private long frame=SystemClock.uptimeMillis(), toastUntil=0;
    private long lastSave=frame;
    private String toast="";
    private float mx,my,scale=.5f;
    private int joyId=-1;

    public FantasyGameView(Context context) {
        super(context);
        d=getResources().getDisplayMetrics().density;
        g=GameProgress.load(context);
        setFocusable(true);
        setLayerType(View.LAYER_TYPE_HARDWARE,null);
    }

    public void pauseGame() {
        g.save(getContext());
        if(page==Page.PLAY) page=Page.PAUSE;
    }
    public void resumeGame() { invalidate(); }
    public boolean handleBackPressed() {
        if(page==Page.PLAY){page=Page.PAUSE;g.save(getContext());invalidate();return true;}
        if(page!=Page.TITLE){page=Page.PLAY;invalidate();return true;}
        return false;
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        long now=SystemClock.uptimeMillis();
        float dt=Math.min(.05f,Math.max(0,(now-frame)/1000f)); frame=now;
        if(page==Page.PLAY) update(dt);
        if(page==Page.TITLE) title(c); else {
            scene(c); hud(c);
            if(now<toastUntil) toast(c);
            if(page==Page.MAP) mapPage(c);
            if(page==Page.JOURNAL) journalPage(c);
            if(page==Page.BAG) bagPage(c);
            if(page==Page.PAUSE) pausePage(c);
        }
        if(now-lastSave>15000){g.save(getContext());lastSave=now;}
        postInvalidateDelayed(16);
    }

    private void update(float dt) {
        float len=(float)Math.sqrt(mx*mx+my*my);
        if(len<.02f)return;
        float speed=420+g.level()*7;
        float nx=clamp(g.playerX+mx/Math.max(1,len)*speed*dt,0,W);
        float ny=clamp(g.playerY+my/Math.max(1,len)*speed*dt,0,H);
        if(walkable(nx,ny)){g.playerX=nx;g.playerY=ny;}
    }

    private boolean walkable(float x,float y) {
        if(dist(x,y,CX,CY)<510 || dist(x,y,SX,SY)<465)return true;
        for(int i=0;i<4;i++){
            if(dist(x,y,RX[i],RY[i])<650)return true;
            if(segment(x,y,CX,CY,RX[i],RY[i])<76)return true;
        }
        return segment(x,y,CX,CY,SX,SY)<76;
    }

    private void scene(Canvas c) {
        float vw=getWidth(),vh=getHeight();
        scale=clamp(Math.min(vw/1450f,vh/700f),.36f,.56f);
        float hx=vw/(2*scale),hy=vh/(2*scale);
        float camX=clamp(g.playerX,hx,W-hx),camY=clamp(g.playerY,hy,H-hy);
        c.drawColor(INK);
        c.save();
        c.translate(vw/2-camX*scale,vh/2-camY*scale);
        c.scale(scale,scale);
        world(c);
        c.restore();
    }

    private void world(Canvas c) {
        Random sea=new Random(17);
        fill(0x556a9fc7);
        for(int i=0;i<105;i++) c.drawCircle(sea.nextFloat()*W,sea.nextFloat()*H,3+sea.nextFloat()*5,p);

        island(c,CX,CY,490,0xff776044,0xffd0a75d);
        island(c,SX,SY,455,0xff314b72,0xff75bce5);
        for(int i=0;i<4;i++) island(c,RX[i],RY[i],650,RC[i],light(RC[i]));

        bridge(c,CX,CY,SX,SY);
        for(int i=0;i<4;i++) bridge(c,CX,CY,RX[i],RY[i]);

        for(int i=0;i<4;i++){
            props(c,i); landmark(c,i);
            text(c,RN[i],RX[i],RY[i]-520,65,WHITE,true,Paint.Align.CENTER);
            shrine(c,RX[i],RY[i],RC[i],g.seals[i]);
            for(int t=0;t<3;t++) rune(c,TX[i][t],TY[i][t],g.tasks[i][t],RC[i]);
        }
        nexus(c); skyGate(c); guide(c); hero(c,g.playerX,g.playerY);
    }

    private void island(Canvas c,float x,float y,float r,int base,int rim) {
        fill(0x8850607a);c.drawOval(x-r,y+r*.62f,x+r,y+r*.9f,p);
        fill(0xff142a42);c.drawCircle(x,y+30,r+34,p);
        fill(rim);c.drawCircle(x,y,r+18,p);
        fill(base);c.drawCircle(x,y,r,p);
        fill(0x33202d45);c.drawOval(x-r*.7f,y-r*.5f,x+r*.58f,y+r*.6f,p);
        stroke(0xfff6d58d,8);c.drawCircle(x,y,r+12,p);
        stroke(0x885ad9f6,4);c.drawCircle(x,y,r-16,p);
    }

    private void bridge(Canvas c,float x1,float y1,float x2,float y2) {
        stroke(0xff172a40,138);p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x1,y1,x2,y2,p);
        stroke(0xffb28a51,104);p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x1,y1,x2,y2,p);
        stroke(0xff5b788a,76);p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x1,y1,x2,y2,p);
        stroke(GOLD,7);p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x1,y1,x2,y2,p);
        p.setStrokeCap(Paint.Cap.BUTT);
    }

    private void props(Canvas c,int region) {
        Random r=new Random(900+region*79);
        for(int i=0;i<38;i++){
            double a=r.nextDouble()*Math.PI*2;
            float rad=(float)Math.sqrt(r.nextFloat())*510;
            float x=RX[region]+(float)Math.cos(a)*rad;
            float y=RY[region]+(float)Math.sin(a)*rad;
            if(region==0) tree(c,x,y,.75f+r.nextFloat()*.6f);
            else if(region==1){
                fill(r.nextBoolean()?0xff653e3c:0xff8b5140);c.drawOval(x-35,y-22,x+38,y+28,p);
                fill(0x88ff7a39);c.drawCircle(x,y-15,8,p);
            } else if(region==2) crystal(c,x,y,32+r.nextFloat()*34,0xffa49aff);
            else ice(c,x,y,42+r.nextFloat()*40);
        }
    }

    private void tree(Canvas c,float x,float y,float s) {
        fill(0xff49382c);c.drawRoundRect(x-12*s,y-5*s,x+12*s,y+65*s,10,10,p);
        fill(0xff236b53);c.drawCircle(x,y-18*s,46*s,p);
        fill(0xff3e9b69);c.drawCircle(x-24*s,y-34*s,29*s,p);c.drawCircle(x+24*s,y-31*s,31*s,p);
        fill(0x557de6a0);c.drawCircle(x-12*s,y-48*s,9*s,p);
    }

    private void crystal(Canvas c,float x,float y,float z,int color) {
        Path a=new Path();a.moveTo(x,y-z);a.lineTo(x+z*.42f,y-z*.12f);a.lineTo(x+z*.18f,y+z);a.lineTo(x-z*.28f,y+z*.75f);a.lineTo(x-z*.42f,y-z*.05f);a.close();
        fill(0xff302d70);c.drawPath(a,p);fill(color);c.drawPath(a,p);
        stroke(0xffe7e5ff,4);c.drawLine(x,y-z+5,x-z*.15f,y+z*.65f,p);
    }

    private void ice(Canvas c,float x,float y,float z) {
        Path a=new Path();a.moveTo(x,y-z);a.lineTo(x+z*.66f,y+z*.55f);a.lineTo(x-z*.65f,y+z*.55f);a.close();
        fill(0xff477d9b);c.drawPath(a,p);
        Path b=new Path();b.moveTo(x,y-z);b.lineTo(x+z*.2f,y-z*.22f);b.lineTo(x,y-z*.02f);b.lineTo(x-z*.18f,y-z*.18f);b.close();
        fill(0xffe6f8ff);c.drawPath(b,p);
    }

    private void landmark(Canvas c,int i) {
        float x=RX[i],y=RY[i]-130;
        if(i==0){tree(c,x,y,2.1f);stroke(0xffe0c27c,10);c.drawCircle(x,y-45,115,p);}
        if(i==1){
            Path a=new Path();a.moveTo(x-230,y+140);a.lineTo(x,y-230);a.lineTo(x+240,y+140);a.close();
            fill(0xff733d3d);c.drawPath(a,p);fill(0xffd85c36);c.drawCircle(x,y-175,55,p);
            stroke(0xffffa34c,15);c.drawLine(x,y-100,x+20,y+120,p);
        }
        if(i==2){crystal(c,x-130,y+35,170,0xff9c8bff);crystal(c,x+15,y-30,235,0xff7ed9ff);crystal(c,x+155,y+65,135,0xffcb9bff);}
        if(i==3){ice(c,x-160,y+25,170);ice(c,x+5,y-55,230);ice(c,x+175,y+35,155);}
    }

    private void rune(Canvas c,float x,float y,boolean done,int color) {
        fill(0x4439d8ff);c.drawCircle(x,y,78,p);
        fill(0xff102b40);c.drawCircle(x,y,47,p);
        stroke(done?0xff80e6ba:light(color),10);c.drawCircle(x,y,44,p);
        Path a=new Path();a.moveTo(x,y-29);a.lineTo(x+25,y);a.lineTo(x,y+29);a.lineTo(x-25,y);a.close();
        fill(done?0xff7e8d99:0xffffe18a);c.drawPath(a,p);
        if(done){text(c,"✓",x,y+13,36,0xff173d43,true,Paint.Align.CENTER);}
    }

    private void shrine(Canvas c,float x,float y,int color,boolean active) {
        fill(0x6653c9ec);c.drawCircle(x,y,178,p);
        fill(0xff20344d);c.drawCircle(x,y,128,p);
        fill(active?0xfff7d779:color);c.drawCircle(x,y,99,p);
        fill(0xff17273d);c.drawCircle(x,y,73,p);
        stroke(active?0xffa9ffe1:0xfff6d58d,8);c.drawCircle(x,y,111,p);
        text(c,active?"✦":"◇",x,y+20,56,active?0xffa9ffe1:0xffffe5a2,true,Paint.Align.CENTER);
    }

    private void nexus(Canvas c) {
        fill(0x5587d9ff);c.drawCircle(CX,CY,390,p);
        stroke(0xffe7bd69,18);c.drawCircle(CX,CY,335,p);
        stroke(0xff58cbe9,9);c.drawCircle(CX,CY,285,p);
        text(c,"N E X U S",CX,CY-410,80,0xffffe8ad,true,Paint.Align.CENTER);
        fill(0x8856d7ff);c.drawCircle(CX,CY,220,p);
        fill(0xffffd77c);c.drawOval(CX-82,CY-130,CX+82,CY+137,p);
        stroke(0xff9e6b2a,7);c.drawOval(CX-82,CY-130,CX+82,CY+137,p);
        text(c,"✧",CX,CY+17,54,0xff93632a,true,Paint.Align.CENTER);
    }

    private void skyGate(Canvas c) {
        fill(0x7753c8ff);c.drawCircle(SX,SY,255,p);
        stroke(0xff6ed5ff,18);c.drawCircle(SX,SY,228,p);
        stroke(GOLD,11);c.drawCircle(SX,SY,185,p);
        fill(0xff293d66);c.drawRoundRect(SX-120,SY-40,SX+120,SY+150,26,26,p);
        stroke(0xffe7bdff,22);c.drawCircle(SX,SY-8,123,p);
        stroke(g.sealCount()==4?GOLD:0xff8291aa,10);c.drawCircle(SX,SY-8,94,p);
        text(c,"GWIEZDNY PORT",SX,SY-300,67,WHITE,true,Paint.Align.CENTER);
    }

    private void guide(Canvas c) {
        float x=2600,y=1765;
        fill(0x665ce5ff);c.drawCircle(x,y,82,p);
        fill(0xff4e9b9b);c.drawRoundRect(x-30,y-8,x+30,y+82,22,22,p);
        fill(0xffffd8a2);c.drawCircle(x,y-37,31,p);
        fill(0xffd5b5ff);c.drawCircle(x-8,y-59,24,p);
        text(c,"AURI",x,y-118,52,0xffffe8ad,true,Paint.Align.CENTER);
    }

    private void hero(Canvas c,float x,float y) {
        fill(0x77313a4c);c.drawOval(x-42,y+31,x+43,y+60,p);
        fill(0xff244a69);c.drawRoundRect(x-30,y-9,x+30,y+65,20,20,p);
        Path a=new Path();a.moveTo(x-25,y+8);a.lineTo(x-58,y+65);a.lineTo(x+58,y+65);a.lineTo(x+25,y+8);a.close();
        fill(0xff267f8b);c.drawPath(a,p);
        fill(0xffffd8ad);c.drawCircle(x,y-31,27,p);
        fill(0xff3b2c40);c.drawArc(x-29,y-59,x+29,y-12,180,180,true,p);
        stroke(0xffffe49a,6);c.drawCircle(x,y-31,34,p);
    }

    private void hud(Canvas c) {
        float w=getWidth(),h=getHeight();
        RectF box=new RectF(12*d,10*d,Math.min(w*.47f,390*d),83*d);
        fill(0xe8152941);c.drawRoundRect(box,16*d,16*d,p);
        text(c,"JUMANJI  •  WORLD OF FANTASY",box.left+12*d,box.top+24*d,16*d,GOLD,true,Paint.Align.LEFT);
        text(c,"POZIOM "+g.level()+"   ESENCJE "+g.essences+"   PIECZĘCIE "+g.sealCount()+"/4",
                box.left+12*d,box.top+50*d,13*d,WHITE,false,Paint.Align.LEFT);
        RectF obj=new RectF(12*d,90*d,Math.min(w*.48f,420*d),140*d);
        fill(0xd9162941);c.drawRoundRect(obj,12*d,12*d,p);
        text(c,"AKTUALNY CEL",obj.left+10*d,obj.top+15*d,10*d,GOLD,true,Paint.Align.LEFT);
        text(c,trim(objective(),40),obj.left+10*d,obj.top+36*d,12*d,WHITE,true,Paint.Align.LEFT);
        button(c,mapRect(),"MAPA",0xff28566d);
        button(c,journalRect(),"ZADANIA",0xff5b4777);
        button(c,bagRect(),"PLECAK",0xff7a593d);
        joystick(c);
        actionButton(c);
        text(c,"DOTKNIJ RUNU, ABY WEJŚĆ W INTERAKCJĘ",w*.5f,h-10*d,10*d,0xffa8bacd,false,Paint.Align.CENTER);
    }

    private void joystick(Canvas c) {
        float x=getWidth()*.15f,y=getHeight()*.78f,r=joyR();
        fill(0x553d7898);c.drawCircle(x,y,r,p);stroke(0xaae3f1ff,3*d);c.drawCircle(x,y,r,p);
        fill(0xff7ca7bb);c.drawCircle(x+mx*r*.5f,y+my*r*.5f,r*.43f,p);
        stroke(0xffe3f1ff,2*d);c.drawCircle(x+mx*r*.5f,y+my*r*.5f,r*.43f,p);
    }

    private void actionButton(Canvas c) {
        float x=getWidth()*.86f,y=getHeight()*.78f,r=actR();
        fill(0x995da6ad);c.drawCircle(x,y,r+8*d,p);
        fill(0xff9d743f);c.drawCircle(x,y,r,p);
        stroke(0xffffe2a0,4*d);c.drawCircle(x,y,r,p);
        text(c,"AKCJA",x,y+5*d,14*d,WHITE,true,Paint.Align.CENTER);
    }

    private void toast(Canvas c) {
        RectF r=new RectF(getWidth()*.27f,getHeight()*.64f,getWidth()*.73f,getHeight()*.73f);
        fill(0xe91c3048);c.drawRoundRect(r,14*d,14*d,p);
        stroke(GOLD,2*d);c.drawRoundRect(r,14*d,14*d,p);
        text(c,trim(toast,65),r.centerX(),r.centerY()+5*d,13*d,WHITE,true,Paint.Align.CENTER);
    }

    private void title(Canvas c) {
        float w=getWidth(),h=getHeight(),cx=w/2,cy=h*.47f;
        c.drawColor(0xff061326);
        Random r=new Random(41);
        for(int i=0;i<130;i++){fill(i%7==0?GOLD:0xff8bbfe2);c.drawCircle(r.nextFloat()*w,r.nextFloat()*h,1+r.nextFloat()*3,p);}
        fill(0xff163a59);c.drawCircle(cx,cy,h*.30f,p);
        stroke(0xff3ba9cc,Math.max(4,h*.012f));c.drawCircle(cx,cy,h*.27f,p);
        stroke(GOLD,Math.max(3,h*.008f));c.drawCircle(cx,cy,h*.22f,p);
        fill(0xffffd77c);c.drawOval(cx-45,cy-75,cx+45,cy+80,p);
        text(c,"JUMANJI II",cx,h*.19f,h*.15f,GOLD,true,Paint.Align.CENTER);
        text(c,"WORLD OF FANTASY",cx,h*.28f,h*.05f,WHITE,true,Paint.Align.CENTER);
        text(c,"Cztery pieczęcie. Pięć krain. Jedna legenda.",cx,h*.72f,h*.03f,0xffbdcfe1,false,Paint.Align.CENTER);
        button(c,new RectF(cx-w*.15f,h*.78f,cx+w*.15f,h*.89f),g.started?"NOWA WYPRAWA":"ROZPOCZNIJ",0xffb8813f);
        if(g.started)button(c,new RectF(cx-w*.15f,h*.91f,cx+w*.15f,h*.99f),"KONTYNUUJ",0xff2e7584);
        text(c,"WERSJA 2.111.1  •  ANDROID 13+",cx,h*.07f,h*.025f,0xff8399b2,false,Paint.Align.CENTER);
    }

    private void mapPage(Canvas c) {
        RectF card=overlay(c,"MAPA ŚWIATA");
        float l=card.left+card.width()*.08f,t=card.top+card.height()*.18f;
        float mw=card.width()*.84f,mh=card.height()*.68f,s=Math.min(mw/W,mh/H);
        float ox=l+(mw-W*s)/2,oy=t+(mh-H*s)/2;
        fill(0xff0b1d34);c.drawRoundRect(l,t,l+mw,t+mh,14*d,14*d,p);
        for(int i=0;i<4;i++)mapDot(c,ox+RX[i]*s,oy+RY[i]*s,Math.max(5*d,620*s),g.seals[i]?0xff72dcb7:RC[i]);
        mapDot(c,ox+CX*s,oy+CY*s,Math.max(5*d,360*s),0xffd0a75d);
        mapDot(c,ox+SX*s,oy+SY*s,Math.max(5*d,230*s),0xff75bce5);
        fill(0xfffff0a6);c.drawCircle(ox+g.playerX*s,oy+g.playerY*s,7*d,p);
    }

    private void mapDot(Canvas c,float x,float y,float r,int color){fill(color);c.drawCircle(x,y,r,p);stroke(0xffffe4a5,2*d);c.drawCircle(x,y,r,p);}

    private void journalPage(Canvas c) {
        RectF card=overlay(c,"DZIENNIK WYPRAWY");
        float x=card.left+28*d,y=card.top+90*d;
        text(c,"GŁÓWNA MISJA",x,y,16*d,GOLD,true,Paint.Align.LEFT);
        wrapped(c,objective(),x,y+32*d,card.width()-56*d,22*d);
        y+=116*d;
        for(int i=0;i<4;i++){
            text(c,RN[i]+"  •  "+(g.seals[i]?"PIECZĘĆ ODBUDOWANA":g.taskCount(i)+"/3 ZNAKÓW"),
                    x,y,13*d,g.seals[i]?0xff8ce8c1:0xffb8c7d7,true,Paint.Align.LEFT);
            y+=29*d;
        }
        text(c,"Postęp zapisuje się automatycznie.",x,card.bottom-28*d,12*d,0xff9cb1c8,false,Paint.Align.LEFT);
    }

    private void bagPage(Canvas c) {
        RectF card=overlay(c,"PLECAK I ZDOBYCZE");
        float x=card.left+30*d,y=card.top+104*d;
        text(c,"✦  ESENCJE: "+g.essences+" / 12",x,y,17*d,GOLD,true,Paint.Align.LEFT);
        text(c,"DOŚWIADCZENIE: "+g.xp+" XP",x,y+42*d,15*d,WHITE,false,Paint.Align.LEFT);
        text(c,"POZIOM ODKRYWCY: "+g.level(),x,y+77*d,15*d,WHITE,false,Paint.Align.LEFT);
        y+=125*d;
        for(int i=0;i<4;i++){
            fill(g.seals[i]?0xff7adab5:0xff62748b);c.drawCircle(x+10*d,y-5*d,9*d,p);
            text(c,RN[i]+(g.seals[i]?" — PIECZĘĆ":" — NIEODKRYTA"),x+32*d,y,13*d,WHITE,false,Paint.Align.LEFT);
            y+=28*d;
        }
    }

    private void pausePage(Canvas c) {
        RectF card=overlay(c,"PAUZA");
        float cx=card.centerX(),y=card.top+card.height()*.48f;
        button(c,new RectF(cx-110*d,y-23*d,cx+110*d,y+23*d),"WRÓĆ DO GRY",0xff2e7584);
        button(c,new RectF(cx-110*d,y+42*d,cx+110*d,y+88*d),"MENU GŁÓWNE",0xff79533d);
        text(c,"Twój postęp jest zapisany.",cx,card.bottom-24*d,12*d,0xffb8c7d7,false,Paint.Align.CENTER);
    }

    private RectF overlay(Canvas c,String title) {
        fill(0xd9000a17);c.drawRect(0,0,getWidth(),getHeight(),p);
        RectF r=new RectF(getWidth()*.17f,getHeight()*.09f,getWidth()*.83f,getHeight()*.91f);
        fill(0xf01a2d45);c.drawRoundRect(r,22*d,22*d,p);
        stroke(0xffd6a75d,3*d);c.drawRoundRect(r,22*d,22*d,p);
        text(c,title,r.centerX(),r.top+42*d,20*d,GOLD,true,Paint.Align.CENTER);
        RectF close=closeRect(r);button(c,close,"X",0xff854d4a);
        return r;
    }

    private void button(Canvas c,RectF r,String label,int color) {
        fill(0x883c526c);c.drawRoundRect(r.left+2*d,r.top+3*d,r.right+2*d,r.bottom+3*d,10*d,10*d,p);
        fill(color);c.drawRoundRect(r,10*d,10*d,p);
        stroke(0xffffdf9b,2*d);c.drawRoundRect(r,10*d,10*d,p);
        text(c,label,r.centerX(),r.centerY()+5*d,Math.min(14*d,r.height()*.34f),WHITE,true,Paint.Align.CENTER);
    }

    private void interact() {
        float x=g.playerX,y=g.playerY;
        if(!g.talkedToGuide && dist(x,y,2600,1765)<190){
            g.talkedToGuide=true;g.xp+=15;notify("Auri: Odnajdź trzy znaki w każdej krainie i obudź jej ołtarz.");save();return;
        }
        for(int i=0;i<4;i++)for(int t=0;t<3;t++){
            if(!g.tasks[i][t] && dist(x,y,TX[i][t],TY[i][t])<155){
                g.tasks[i][t]=true;g.essences++;g.xp+=25;notify("Znak światła odnaleziony. +25 XP");save();return;
            }
        }
        for(int i=0;i<4;i++){
            if(g.taskCount(i)==3&&!g.seals[i]&&dist(x,y,RX[i],RY[i])<175){
                g.seals[i]=true;g.xp+=100;notify("Odbudowano pieczęć: "+RN[i]);save();return;
            }
        }
        if(dist(x,y,SX,SY)<205){
            if(g.sealCount()==4){
                if(!g.completed){g.completed=true;g.xp+=250;notify("Portal otwarty! Przywrócono równowagę świata.");}
                else notify("Nexus jest bezpieczny. Odkrywaj dalej.");
            } else notify("Portal wymaga czterech pieczęci. Odbuduj ołtarze wszystkich krain.");
            save();return;
        }
        if(dist(x,y,CX,CY)<210){notify("Nexus łączy krainy. Auri czeka niedaleko.");return;}
        notify("Podejdź bliżej świecącego znaku lub postaci.");
    }

    private String objective() {
        if(!g.talkedToGuide)return "Porozmawiaj z Auri przy centralnym Nexusie.";
        for(int i=0;i<4;i++)if(!g.seals[i]){
            if(g.taskCount(i)<3)return QUEST[i]+" — "+g.taskCount(i)+"/3";
            return "Aktywuj ołtarz: "+RN[i];
        }
        return g.completed?"Świat odzyskał równowagę. Odkrywaj dalej.":"Otwórz portal w Gwiezdnym Porcie.";
    }

    private void notify(String s){toast=s;toastUntil=SystemClock.uptimeMillis()+4200;invalidate();}
    private void save(){g.save(getContext());lastSave=SystemClock.uptimeMillis();}
    private void fill(int color){p.reset();p.setAntiAlias(true);p.setColor(color);p.setStyle(Paint.Style.FILL);}
    private void stroke(int color,float width){p.reset();p.setAntiAlias(true);p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(width);p.setStrokeJoin(Paint.Join.ROUND);}
    private void text(Canvas c,String s,float x,float y,float size,int color,boolean bold,Paint.Align align){
        p.reset();p.setAntiAlias(true);p.setColor(color);p.setTextSize(size);p.setTextAlign(align);
        p.setTypeface(Typeface.create("sans-serif-condensed",bold?Typeface.BOLD:Typeface.NORMAL));c.drawText(s,x,y,p);
    }
    private void wrapped(Canvas c,String s,float x,float y,float max,float lineHeight){
        p.reset();p.setAntiAlias(true);p.setColor(WHITE);p.setTextSize(15*d);p.setTypeface(Typeface.DEFAULT);
        StringBuilder line=new StringBuilder();
        for(String word:s.split(" ")){
            String next=line.length()==0?word:line+" "+word;
            if(p.measureText(next)>max&&line.length()>0){c.drawText(line.toString(),x,y,p);y+=lineHeight;line=new StringBuilder(word);}
            else line=new StringBuilder(next);
        }
        if(line.length()>0)c.drawText(line.toString(),x,y,p);
    }
    private RectF mapRect(){return topButton(0);}
    private RectF journalRect(){return topButton(1);}
    private RectF bagRect(){return topButton(2);}
    private RectF topButton(int index){
        float bw=Math.min(getWidth()*.105f,88*d),bh=Math.min(getHeight()*.15f,52*d),right=getWidth()-12*d,gap=7*d;
        float left=right-(3-index)*bw-(2-index)*gap;
        return new RectF(left,13*d,left+bw,13*d+bh);
    }
    private RectF closeRect(RectF card){float z=42*d;return new RectF(card.right-z-16*d,card.top+12*d,card.right-16*d,card.top+12*d+z);}
    private float joyR(){return Math.min(getWidth()*.12f,getHeight()*.25f);}
    private float actR(){return Math.min(getWidth()*.105f,getHeight()*.22f);}
    private static float dist(float x,float y,float a,float b){float dx=x-a,dy=y-b;return (float)Math.sqrt(dx*dx+dy*dy);}
    private static float segment(float x,float y,float ax,float ay,float bx,float by){
        float dx=bx-ax,dy=by-ay,den=dx*dx+dy*dy;if(den==0)return dist(x,y,ax,ay);
        float t=clamp(((x-ax)*dx+(y-ay)*dy)/den,0,1);return dist(x,y,ax+t*dx,ay+t*dy);
    }
    private static float clamp(float v,float lo,float hi){return Math.max(lo,Math.min(hi,v));}
    private static int light(int c){return Color.rgb(Math.min(255,Color.red(c)+45),Math.min(255,Color.green(c)+45),Math.min(255,Color.blue(c)+45));}
    private static String trim(String s,int max){return s.length()<=max?s:s.substring(0,max-1)+"…";}

    private void newGame(){g.reset();page=Page.PLAY;notify("Witaj w Jumanji. Auri czeka przy Nexusie.");save();}
    private void moveJoystick(float x,float y){
        float cx=getWidth()*.15f,cy=getHeight()*.78f,r=joyR(),dx=x-cx,dy=y-cy,len=(float)Math.sqrt(dx*dx+dy*dy);
        if(len>r){dx*=r/len;dy*=r/len;}mx=dx/r;my=dy/r;
    }

    @Override public boolean onTouchEvent(MotionEvent e){
        float x=e.getX(),y=e.getY();int a=e.getActionMasked();
        if(a==MotionEvent.ACTION_DOWN){
            if(page==Page.TITLE){
                float cx=getWidth()*.5f,h=getHeight();
                if(y>=h*.76f&&y<=h*.90f&&Math.abs(x-cx)<getWidth()*.18f)newGame();
                else if(g.started&&y>=h*.90f&&Math.abs(x-cx)<getWidth()*.18f){page=Page.PLAY;notify("Witaj ponownie.");}
                invalidate();return true;
            }
            if(page==Page.MAP||page==Page.JOURNAL||page==Page.BAG){
                RectF card=new RectF(getWidth()*.17f,getHeight()*.09f,getWidth()*.83f,getHeight()*.91f);
                if(closeRect(card).contains(x,y)||!card.contains(x,y))page=Page.PLAY;
                invalidate();return true;
            }
            if(page==Page.PAUSE){
                float cx=getWidth()*.5f,cy=getHeight()*.09f+getHeight()*.82f*.48f;
                if(Math.abs(x-cx)<140*d&&y>cy-35*d&&y<cy+42*d)page=Page.PLAY;
                else if(Math.abs(x-cx)<140*d&&y>=cy+42*d)page=Page.TITLE;
                else page=Page.PLAY;
                invalidate();return true;
            }
            if(page==Page.PLAY){
                if(mapRect().contains(x,y)){page=Page.MAP;invalidate();return true;}
                if(journalRect().contains(x,y)){page=Page.JOURNAL;invalidate();return true;}
                if(bagRect().contains(x,y)){page=Page.BAG;invalidate();return true;}
                if(dist(x,y,getWidth()*.86f,getHeight()*.78f)<actR()*1.35f){interact();return true;}
                if(x<getWidth()*.32f&&y>getHeight()*.55f){
                    joyId=e.getPointerId(e.getActionIndex());moveJoystick(x,y);return true;
                }
            }
        } else if(a==MotionEvent.ACTION_MOVE&&joyId!=-1){
            int index=e.findPointerIndex(joyId);if(index>=0)moveJoystick(e.getX(index),e.getY(index));return true;
        } else if(a==MotionEvent.ACTION_UP||a==MotionEvent.ACTION_POINTER_UP||a==MotionEvent.ACTION_CANCEL){
            if(joyId!=-1&&(a==MotionEvent.ACTION_CANCEL||a==MotionEvent.ACTION_UP||e.getPointerId(e.getActionIndex())==joyId)){
                joyId=-1;mx=0;my=0;invalidate();
            }return true;
        }
        return true;
    }
}
