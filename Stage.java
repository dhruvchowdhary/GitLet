package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

public class Stage implements Serializable {
    /** The stage's addition hashmap. */
    private HashMap<String, String> addition;
    /** The stage's removal hashmap. */
    private HashMap<String, String> removal;
    /** The stage file. */
    static final File STAGE = new File(".gitlet/stage.txt");
    /** The cwd file. */
    static final File CWD = new File(System.getProperty("user.dir"));

    public Stage() {
        this.addition = new HashMap<String, String>();
        this.removal = new HashMap<String, String>();
    }

    public void add(String[] args) {
        File stageFile = new File(".gitlet/stage.txt");
        Stage currentStage = Utils.readObject(stageFile, Stage.class);
        HashMap<String, String> stageAddition = currentStage.getAddition();
        HashMap<String, String> stageRemoval = currentStage.getRemoval();
        for (int i = 1; i < args.length; i++) {
            File fileAdd = new File(CWD + "/" + args[i]);
            if (!fileAdd.exists()) {
                System.out.println("File does not exist.");
                return;
            }
            String content = Utils.readContentsAsString(fileAdd);
            if (Main.getHeadCommit().getSelfFiles() != null) {
                if (Main.getHeadCommit().getSelfFiles().containsKey(args[i])) {
                    Commit headCommit = Main.getHeadCommit();
                    String stuff = headCommit.getSelfFiles().get(args[i]);
                    if (stuff.equals(content)) {
                        if (stageRemoval.containsKey(args[i])) {
                            stageRemoval.remove(args[i]);
                        }
                        Utils.writeObject(STAGE, this);
                        continue;
                    }
                }
            }
            if (!stageAddition.containsKey(args[i])) {
                stageAddition.put(args[i], content);
            } else {
                stageAddition.replace(args[i], content);
            }
            if (stageRemoval.containsKey(args[i])) {
                stageRemoval.remove(args[i]);
                stageAddition.remove(args[i]);
            }
            addition = stageAddition;
            removal = stageRemoval;
            Utils.writeObject(STAGE, this);
        }
    }

    public void remove(String[] args) {
        File stageFile = new File(".gitlet/stage.txt");
        Stage currentStage = Utils.readObject(stageFile, Stage.class);
        HashMap<String, String> stageAddition = currentStage.getAddition();
        HashMap<String, String> stageRemoval = currentStage.getRemoval();
        Commit headCommit = Main.getHeadCommit();
        HashMap<String, String> headFiles = headCommit.getSelfFiles();
        for (int i = 1; i < args.length; i++) {
            File fileRemove = new File(CWD + "/" + args[i]);
            String content = "";
            if (headFiles != null) {
                if (headFiles.containsKey(args[i])) {
                    if (fileRemove.exists()) {
                        content = Utils.readContentsAsString(fileRemove);
                        fileRemove.delete();
                    }
                    stageRemoval.put(args[i], content);
                } else {
                    System.out.println("No reason to remove the file.");
                }
            } else if (!stageAddition.containsKey(args[i])) {
                System.out.println("No reason to remove the file.");
            }
            if (stageAddition.containsKey(args[i])) {
                stageAddition.remove(args[i]);
            }
        }
        addition = stageAddition;
        removal = stageRemoval;
        Utils.writeObject(STAGE, this);
    }

    public HashMap<String, String> getAddition() {
        return addition;
    }

    public HashMap<String, String> getRemoval() {
        return removal;
    }
}
