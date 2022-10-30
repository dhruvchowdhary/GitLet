package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Commit implements Serializable {
    /** The parent of the commit. */
    private String parent;
    /** The message of the commit. */
    private String message;
    /** The timestamp of the commit. */
    private String timestamp;
    /** The files of the parent commit. */
    private HashMap<String, String> parentFiles;
    /** The files of the commit. */
    private HashMap<String, String> selfFiles;

    public Commit(String message2, String parent2) {
        this.message = message2;
        this.parent = parent2;
        if (this.parent == null) {
            this.timestamp = "Wed Dec 31 16:00:00 1969 -0700";
        } else {
            String format = "EEE MMM dd HH:mm:ss yyyy Z";
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            this.timestamp = sdf.format(new Date());
            File parentFile = new File(".gitlet/commits/" + parent2 + ".txt");
            Commit parentCommit = Utils.readObject(parentFile, Commit.class);
            parentFiles = parentCommit.getParentFiles();
        }
    }

    public void saveFiles(HashMap<String, String> files) {
        selfFiles = files;
    }

    public void saveCommit() {
        String encode = Utils.sha1(timestamp + " " + message);
        File commit = new File(".gitlet/commits/" + encode + ".txt");
        Utils.writeObject(commit, this);

        File head = new File(".gitlet/commits/head.txt");
        Utils.writeObject(head, this);

        File currentBranchFile = new File(".gitlet/branches/current.txt");
        String branchName = Utils.readContentsAsString(currentBranchFile);

        File listFil = new File(".gitlet/branches/list.txt");
        File globalListFile = new File(".gitlet/branches/globallist.txt");
        HashMap<String, Commit> list = Utils.readObject(listFil, HashMap.class);
        list.replace(branchName, this);
        Utils.writeObject(listFil, list);
        Utils.writeObject(globalListFile, list);

        File findFile = new File(".gitlet/commits/find.txt");
        HashMap findList = Utils.readObject(findFile, HashMap.class);
        findList.put(Utils.sha1(timestamp + " " + message), message);
        Utils.writeObject(findFile, findList);

        Main.clearStage();
    }

    public static Commit fromFile(String name) {
        File commit = new File(".gitlet/commits/" + name + ".txt");
        return Utils.readObject(commit, Commit.class);
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getParent() {
        return parent;
    }

    public HashMap<String, String> getParentFiles() {
        return parentFiles;
    }

    public HashMap<String, String> getSelfFiles() {
        return selfFiles;
    }

}
