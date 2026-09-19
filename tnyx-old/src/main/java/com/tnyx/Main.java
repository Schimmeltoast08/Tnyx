package com.tnyx;

import com.tnyx.crypto.OpenedVault;
import com.tnyx.ui.ThemeManager;
import com.tnyx.ui.UiManager;
import com.tnyx.vault.Vault;
import com.tnyx.vault.VaultHandler;

import javax.swing.SwingUtilities;
import java.io.Console;
import java.io.IOException;
import java.util.Arrays;

import static com.tnyx.util.Log.log;

public final class Main {
    private static final int TNYX_MAIN_VERSION = 1;
    private Main() {}

    public static void main(String[] args) {
        log("=== Starting application v" + TNYX_MAIN_VERSION + " ===", 2);
        if (args.length == 0 || "--gui".equals(args[0])) { gui(); return; }
        switch (args[0]) {
            case "--new" -> newVault(args);
            case "--edit" -> edit(args);
            case "--remove" -> remove(args);
            case "--add" -> add(args);
            case "--open" -> open(args);
            default -> { log("Unknown command", 3); printUsage(); }
        }
    }

    private static void edit(String[] args) {
        requireArg(args, "--edit");
        char[] password = getPassword();
        try { VaultHandler.editEntry(args[1], password); }
        catch (IOException e) { log("Could not edit password entry: " + e.getMessage(), 4); }
        finally { Arrays.fill(password, '\0'); }
    }

    private static void remove(String[] args) {
        requireArg(args, "--remove");
        char[] password = getPassword();
        try { VaultHandler.removeEntry(args[1], password); }
        catch (IOException e) { log("Could not remove password entry: " + e.getMessage(), 4); }
        finally { Arrays.fill(password, '\0'); }
    }

    private static void add(String[] args) {
        Console console = requireConsole();
        String filepath = args.length > 1 ? args[1] : console.readLine("Filepath: ");
        char[] masterPW = console.readPassword("Vault Master Password: ");
        char[] pw = console.readPassword("Password: ");
        try {
            String name = console.readLine("Entry name: ");
            String username = console.readLine("Username: ");
            String url = console.readLine("URL: ");
            VaultHandler.addVaultEntry(filepath, name, username, pw, url, masterPW);
        } catch (IOException e) { log("Could not add password entry: " + e.getMessage(), 4); }
        finally { Arrays.fill(masterPW, '\0'); Arrays.fill(pw, '\0'); }
    }

    private static void open(String[] args) {
        requireArg(args, "--open");
        char[] password = getPassword();
        try (Vault vault = VaultHandler.decryptVault(args[1], password)) { vault.printVault(); }
        catch (IOException e) { log("Could not read vault: " + e.getMessage(), 4); }
        finally { Arrays.fill(password, '\0'); }
    }

    private static void newVault(String[] args) {
        requireArg(args, "--new");
        char[] password = getPassword();
        try (OpenedVault ignored = VaultHandler.createEncryptedVault(args[1], password)) { log("Created new vault", 2); }
        catch (IOException e) { log("Failed to create vault: " + e.getMessage(), 4); }
        finally { Arrays.fill(password, '\0'); }
    }

    private static char[] getPassword() { return requireConsole().readPassword("Master password: "); }
    private static Console requireConsole() {
        Console console = System.console();
        if (console == null) throw new IllegalStateException("Password entry requires a real console");
        return console;
    }
    private static void requireArg(String[] args, String command) { if (args.length < 2) throw new IllegalArgumentException("Missing vault path for " + command); }
    private static void printUsage() { System.out.println("Usage: tnyx [--gui|--new PATH|--open PATH|--add PATH|--edit PATH|--remove PATH]"); }

    private static void gui() {
        SwingUtilities.invokeLater(() -> {
            try { ThemeManager.setDark(); } catch (Exception e) { log("Could not set theme to dark", 3); }
            new UiManager();
        });
    }

    public static void exitApplication(int code) {
        UiManager.shutdown();
        System.exit(code);
    }
}
