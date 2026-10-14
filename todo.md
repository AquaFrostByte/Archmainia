[kira@theWired Archmainia]$ java Main.java
/mnt/MemCard/Personal/Code/Archmainia/MenuPanel.java:47: Fehler: Symbol nicht gefunden
                    String grade = Main.saveSystem.getGrade(levelNa);
                                                            ^
  Symbol: Variable levelNa
  Ort: Klasse MenuPanel
/mnt/MemCard/Personal/Code/Archmainia/GamePanel.java:127: Fehler: Symbol nicht gefunden
            audioManager.setVolume(Main.saveSystem.volume);
                                                  ^
  Symbol: Variable volume
  Ort: Variable saveSystem von Typ SaveSystem
/mnt/MemCard/Personal/Code/Archmainia/GamePanel.java:238: Fehler: Symbol nicht gefunden
        bindKey("D", 0);
        ^
  Symbol: Methode bindKey(String,int)
  Ort: Klasse GamePanel
/mnt/MemCard/Personal/Code/Archmainia/GamePanel.java:239: Fehler: Symbol nicht gefunden
        bindKey("F", 1);
        ^
  Symbol: Methode bindKey(String,int)
  Ort: Klasse GamePanel
/mnt/MemCard/Personal/Code/Archmainia/GamePanel.java:240: Fehler: Symbol nicht gefunden
        bindKey("H", 2);
        ^
  Symbol: Methode bindKey(String,int)
  Ort: Klasse GamePanel
/mnt/MemCard/Personal/Code/Archmainia/GamePanel.java:241: Fehler: Symbol nicht gefunden
        bindKey("J", 3);
        ^
  Symbol: Methode bindKey(String,int)
  Ort: Klasse GamePanel
6 Fehler
Fehler: Kompilierung nicht erfolgreich
[kira@theWired Archmainia]$ 

add the key setup

Also Settings.javaS