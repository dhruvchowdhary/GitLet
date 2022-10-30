package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;


/** Driver class for Gitlet, the tiny stupid version-control system.
 * @author dhruvchowdhary
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    static final File GITLET = new File(".gitlet");
    /** The branches directory. */
    static final File BRANCHES = new File(".gitlet/branches");
    /** The commits directory. */
    static final File COMMITS = new File(".gitlet/commits");
    /** Creates a new empty stage to use. */
    static final Stage STAGEAREA = new Stage();
    /** No commit error text. */
    static final String NOCOMMITERR = "No commit with that id exists.";
    /** File does not exist error text. */
    static final String NOFILEERR = "File does not exist in that commit.";
    /** First half of untracked file error text. */
    static final String W = "There is an untracked file in the way; ";
    /** Untracked file error text. */
    static final String E = W + "delete it, or add and commit it first.";

    public static void main(String... args) {
        if (args[0].equals("init")) {
            init();
        } else if (args[0].equals("add")) {
            add(args);
        } else if (args[0].equals("commit")) {
            commit(args);
        } else if (args[0].equals("rm")) {
            rm(args);
        } else if (args[0].equals("log")) {
            log();
        } else if (args[0].equals("global-log")) {
            globallog();
        } else if (args[0].equals("reset")) {
            reset(args);
        } else if (args[0].equals("checkout")) {
            checkout(args);
        } else if (args[0].equals("find")) {
            find(args);
        } else if (args[0].equals("status")) {
            status();
        } else if (args[0].equals("branch")) {
            branch(args);
        } else if (args[0].equals("rm-branch")) {
            rmbranch(args);
        } else if (args[0].equals("merge")) {
            merge(args);
        } else {
            System.out.println("No command with that name exists.");
        }
    }

    public static void init() {
        if (!GITLET.exists()) {
            GITLET.mkdir();
            Commit initial = new Commit("initial commit", null);

            BRANCHES.mkdir();
            File currentBranchFile = new File(".gitlet/branches/current.txt");
            Utils.writeContents(currentBranchFile, "master");

            HashMap<String, Commit> list = new HashMap<String, Commit>();
            list.put("master", initial);
            File listFile = new File(".gitlet/branches/list.txt");
            File globalListFile = new File(".gitlet/branches/globallist.txt");
            Utils.writeObject(listFile, list);
            Utils.writeObject(globalListFile, list);

            COMMITS.mkdir();
            HashMap<String, String> findList = new HashMap();
            String date = "Wed Dec 31 16:00:00 1969 -0700";
            String code = date + " " + "initial commit";
            findList.put(Utils.sha1(code), "initial commit");
            File findFile = new File(".gitlet/commits/find.txt");
            Utils.writeObject(findFile, findList);

            initial.saveCommit();
        } else {
            String v = "A Gitlet version-control system alr";
            String d = "eady exists in the current directory.";
            System.out.println(v + d);
        }
    }

    public static void add(String[] args) {
        STAGEAREA.add(args);
    }

    public static void rm(String[] args) {
        STAGEAREA.remove(args);
    }

    public static void commit(String[] args) {
        if (!args[1].isEmpty()) {
            Commit parentCommit = getHeadCommit();
            String pTime = parentCommit.getTimestamp();
            String pMessage = parentCommit.getMessage();
            String parent2 = Utils.sha1(pTime + " " + pMessage);
            Commit commit = new Commit(args[1], parent2);

            File stageFile = new File(".gitlet/stage.txt");
            Stage c = Utils.readObject(stageFile, Stage.class);
            HashMap<String, String> stageAdd = c.getAddition();
            HashMap<String, String> stageRemove = c.getRemoval();

            HashMap<String, String> parentFiles = commit.getParentFiles();
            parentFiles = getHeadCommit().getSelfFiles();
            HashMap<String, String> commitFiles = new HashMap();
            if (parentFiles != null) {
                commitFiles = parentFiles;
            }
            if (!c.getAddition().isEmpty() || !c.getRemoval().isEmpty()) {
                HashMap<String, String> finalCommitFiles = commitFiles;
                stageAdd.forEach((key, value) -> {
                    if (finalCommitFiles != null) {
                        if (finalCommitFiles.containsKey(key)) {
                            finalCommitFiles.replace(key, value);
                        } else {
                            finalCommitFiles.put(key, value);
                        }
                    } else {
                        finalCommitFiles.put(key, value);
                    }
                });
                stageRemove.forEach((key, value) -> {
                    if (finalCommitFiles != null) {
                        if (finalCommitFiles.containsKey(key)) {
                            finalCommitFiles.remove(key);
                        }
                    }
                });
                commit.saveFiles(finalCommitFiles);
                commit.saveCommit();
            } else {
                System.out.println("No changes added to the commit.");
            }
        } else {
            System.out.println("Please enter a commit message.");
        }
    }

    public static void log() {
        Commit headCommit = getHeadCommit();
        System.out.println("===");
        String hTime = headCommit.getTimestamp();
        String hMessage = headCommit.getMessage();
        System.out.println("commit " + Utils.sha1(hTime + " " + hMessage));
        System.out.println("Date: " + headCommit.getTimestamp());
        System.out.println(headCommit.getMessage());
        System.out.println();
        while (headCommit.getParent() != null) {
            String f = headCommit.getParent();
            File p = new File(".gitlet/commits/" + f + ".txt");
            headCommit = Utils.readObject(p, Commit.class);
            System.out.println("===");
            String hTime2 = headCommit.getTimestamp();
            String hMessage2 = headCommit.getMessage();
            String j = Utils.sha1(hTime2 + " " + hMessage2);
            System.out.println("commit " + j);
            System.out.println("Date: " + headCommit.getTimestamp());
            System.out.println(headCommit.getMessage());
            System.out.println();
        }
    }

    public static void globallog() {
        File globalListFile = new File(".gitlet/branches/globallist.txt");
        HashMap list = Utils.readObject(globalListFile, HashMap.class);
        Commit masterHead = (Commit) list.get("master");
        System.out.println("===");
        String time = masterHead.getTimestamp();
        String mes = masterHead.getMessage();
        System.out.println("commit " + Utils.sha1(time + " " + mes));
        System.out.println("Date: " + masterHead.getTimestamp());
        System.out.println(masterHead.getMessage());
        System.out.println();
        while (masterHead.getParent() != null) {
            String p = masterHead.getParent();
            File p2 = new File(".gitlet/commits/" + p + ".txt");
            masterHead = Utils.readObject(p2, Commit.class);
            System.out.println("===");
            String t = masterHead.getTimestamp();
            String m = masterHead.getMessage();
            System.out.println("commit " + Utils.sha1(t + " " + m));
            System.out.println("Date: " + masterHead.getTimestamp());
            System.out.println(masterHead.getMessage());
            System.out.println();
        }
    }

    public static void reset(String[] args) {
        File commitResetFile = new File(".gitlet/commits/" + args[1] + ".txt");
        File s = new File(".gitlet/stage.txt");

        if (commitResetFile.exists()) {
            Commit commitRese = Utils.readObject(commitResetFile, Commit.class);
            HashMap additions = Utils.readObject(s, Stage.class).getAddition();
            for (File f : CWD.listFiles()) {
                if (getHeadCommit().getSelfFiles() != null) {
                    HashMap c = getHeadCommit().getSelfFiles();
                    if (!c.containsKey(f.getName())) {
                        if (!additions.containsKey(f.getName())) {
                            if (!f.getName().equals(".gitlet")) {
                                if (!f.getName().equals("gitlet")) {
                                    System.out.println(E);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
            File currentBranchFile = new File(".gitlet/branches/current.txt");
            String branchName = Utils.readContentsAsString(currentBranchFile);
            File listFil = new File(".gitlet/branches/list.txt");
            HashMap list = Utils.readObject(listFil, HashMap.class);
            list.replace(branchName, commitRese);
            Utils.writeObject(listFil, list);

            for (File file : CWD.listFiles()) {
                if (!file.getName().equals(".gitlet")) {
                    if (!file.getName().equals("gitlet")) {
                        file.delete();
                    }
                }
            }
            getHeadCommit().getSelfFiles().forEach((key, value) -> {
                File newFile = new File(key);
                try {
                    newFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Utils.writeContents(newFile, value);
            });

            clearStage();
        } else {
            System.out.println(NOCOMMITERR);
        }
    }

    public static void checkout(String[] args) {
        if (args[1].equals("--")) {
            Commit headCommit = getHeadCommit();
            HashMap<String, String> headFiles = headCommit.getSelfFiles();
            if (headFiles.containsKey(args[2])) {
                String prop = System.getProperty("user.dir");
                File c = new File(prop + "/" + args[2]);
                Utils.writeContents(c, headFiles.get(args[2]));
            } else {
                System.out.println(NOFILEERR);
            }
        } else if (args.length > 2) {
            if (args[2].equals("--")) {
                File commitsFolder = new File(".gitlet/commits");
                String commitID = args[1];
                String t = getHeadCommit().getTimestamp();
                String m = getHeadCommit().getMessage();
                if (args[1].length() < Utils.sha1(t + " " + m).length()) {
                    boolean exists = false;
                    for (File f : commitsFolder.listFiles()) {
                        if (f.getName().startsWith(args[1])) {
                            exists = true;
                            int n = f.getName().length() - 4;
                            commitID = f.getName().substring(0, n);
                        }
                    }
                    if (!exists) {
                        System.out.println(NOCOMMITERR);
                        return;
                    }
                }
                checkoutHelper3(args[3], commitID);
            } else {
                System.out.println("Incorrect operands.");
            }
        } else {
            File d = new File(".gitlet/branches/list.txt");
            HashMap<String, Commit> list = Utils.readObject(d, HashMap.class);
            if (list.containsKey(args[1])) {
                File currBranchFile = new File(".gitlet/branches/current.txt");
                String branchName = Utils.readContentsAsString(currBranchFile);
                if (branchName.equals(args[1])) {
                    String n = "No need to checkout the current branch.";
                    System.out.println(n);
                } else {
                    checkoutHelper2(args[1]);
                    Utils.writeContents(currBranchFile, args[1]);
                    checkoutHelper();
                }
            } else {
                System.out.println("No such branch exists.");
            }
        }
    }

    public static void checkoutHelper3(String arg, String commitID) {
        File c = new File(".gitlet/commits/" + commitID + ".txt");
        if (c.exists()) {
            Commit cc = Utils.readObject(c, Commit.class);
            HashMap f = cc.getSelfFiles();
            if (f.containsKey(arg)) {
                String l = System.getProperty("user.dir");
                File g = new File(l + "/" + arg);
                Utils.writeContents(g, f.get(arg));
            } else {
                System.out.println(NOFILEERR);
            }
        } else {
            System.out.println(NOCOMMITERR);
        }
    }

    public static void checkoutHelper2(String arg) {
        File d = new File(".gitlet/branches/list.txt");
        HashMap<String, Commit> list = Utils.readObject(d, HashMap.class);
        File stageFile = new File(".gitlet/stage.txt");
        Stage n = Utils.readObject(stageFile, Stage.class);
        HashMap additions = n.getAddition();
        for (File f : CWD.listFiles()) {
            if (getHeadCommit().getSelfFiles() != null) {
                if (!additions.containsKey(f.getName())) {
                    HashMap h = getHeadCommit().getSelfFiles();
                    if (!h.containsKey(f.getName())) {
                        if (!f.getName().equals(".gitlet")) {
                            if (!f.getName().equals("gitlet")) {
                                System.out.println(E);
                                return;
                            }
                        }
                    }
                }
            } else if (list.get(arg).getSelfFiles() != null) {
                System.out.println(E);
                return;
            }
        }
    }

    public static void checkoutHelper() {
        if (getHeadCommit().getSelfFiles() != null) {
            getHeadCommit().getSelfFiles().forEach((key, value) -> {
                File file = new File(key);
                Utils.writeContents(file, value);
            });
        }
        for (File file : CWD.listFiles()) {
            if (getHeadCommit().getSelfFiles() != null) {
                HashMap h = getHeadCommit().getSelfFiles();
                if (!h.containsKey(file.getName())) {
                    file.delete();
                }
            } else {
                file.delete();
            }
        }
        clearStage();
    }

    public static void status() {
        System.out.println("=== Branches ===");
        File currentBranchFile = new File(".gitlet/branches/current.txt");
        String branchName = Utils.readContentsAsString(currentBranchFile);
        File listFil = new File(".gitlet/branches/list.txt");
        HashMap<String, Commit> list = Utils.readObject(listFil, HashMap.class);
        ArrayList<String> branches = new ArrayList<String>();
        list.forEach((key, value) -> {
            branches.add(key);
        });
        Collections.sort(branches);
        for (String branch : branches) {
            if (branch.equals(branchName)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        File stageFile = new File(".gitlet/stage.txt");
        ArrayList<String> additions = new ArrayList<String>();
        if (stageFile.exists()) {
            Stage currentStage = Utils.readObject(stageFile, Stage.class);
            HashMap<String, String> stageAddition = currentStage.getAddition();
            stageAddition.forEach((key, value) -> {
                additions.add(key);
            });
            Collections.sort(additions);
            for (String addition : additions) {
                System.out.println(addition);
            }
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        ArrayList<String> removals = new ArrayList<String>();
        if (stageFile.exists()) {
            Stage currentStage = Utils.readObject(stageFile, Stage.class);
            HashMap<String, String> stageRemoved = currentStage.getRemoval();
            stageRemoved.forEach((key, value) -> {
                removals.add(key);
            });
            Collections.sort(removals);
            for (String removal : removals) {
                System.out.println(removal);
            }
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
    }

    public static void find(String[] args) {
        File findFil = new File(".gitlet/commits/find.txt");
        HashMap findList = Utils.readObject(findFil, HashMap.class);
        AtomicBoolean exists = new AtomicBoolean(false);
        findList.forEach((key, value) -> {
            if (value.equals(args[1])) {
                exists.set(true);
                System.out.println(key);
            }
        });
        if (!exists.get()) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void branch(String[] args) {
        File listFil = new File(".gitlet/branches/list.txt");
        File globalListFile = new File(".gitlet/branches/globallist.txt");
        HashMap list = Utils.readObject(listFil, HashMap.class);

        if (list.containsKey(args[1])) {
            System.out.println("A branch with that name already exists.");
        } else {
            list.put(args[1], getHeadCommit());
        }
        Utils.writeObject(listFil, list);
        Utils.writeObject(globalListFile, list);
    }

    public static void rmbranch(String[] args) {
        File listFil = new File(".gitlet/branches/list.txt");
        HashMap list = Utils.readObject(listFil, HashMap.class);

        File currentBranchFile = new File(".gitlet/branches/current.txt");
        String branchName = Utils.readContentsAsString(currentBranchFile);

        if (!list.containsKey(args[1])) {
            System.out.println("A branch with that name does not exist.");
        } else if (args[1].equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
        } else {
            list.remove(args[1]);
            Utils.writeObject(listFil, list);
        }
    }

    public static void merge(String[] args) {

    }

    public static Commit getHeadCommit() {
        File currentBranchFile = new File(".gitlet/branches/current.txt");
        String branchName = Utils.readContentsAsString(currentBranchFile);

        File listFil = new File(".gitlet/branches/list.txt");
        HashMap list = Utils.readObject(listFil, HashMap.class);

        return (Commit) list.get(branchName);
    }

    public static void clearStage() {
        File stageFile = new File(".gitlet/stage.txt");
        Stage newStage = new Stage();
        Utils.writeObject(stageFile, newStage);
    }
}
