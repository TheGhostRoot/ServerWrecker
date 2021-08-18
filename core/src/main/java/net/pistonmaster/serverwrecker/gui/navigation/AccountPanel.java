package net.pistonmaster.serverwrecker.gui.navigation;

import net.pistonmaster.serverwrecker.ServerWrecker;
import net.pistonmaster.serverwrecker.common.ProxyType;
import net.pistonmaster.serverwrecker.common.ServiceServer;
import net.pistonmaster.serverwrecker.gui.LoadAccountsListener;
import net.pistonmaster.serverwrecker.gui.LoadProxiesListener;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.Arrays;
import java.util.Objects;

public class AccountPanel extends NavigationItem {
    public static final JComboBox<ProxyType> proxyTypeCombo = new JComboBox<>();
    public static final JSpinner accPerProxy = new JSpinner();

    public AccountPanel(ServerWrecker wireBot, JFrame parent) {
        JPanel accounts = new JPanel();

        JButton loadAccounts = new JButton("Load Accounts");

        JFileChooser accountChooser = new JFileChooser();
        accountChooser.addChoosableFileFilter(new FileNameExtensionFilter("", "txt"));
        loadAccounts.addActionListener(new LoadAccountsListener(wireBot, parent, accountChooser));

        JComboBox<ServiceServer> serviceBox = new JComboBox<>();
        Arrays.stream(ServiceServer.values()).forEach(serviceBox::addItem);

        serviceBox.setSelectedItem(ServiceServer.MOJANG);

        serviceBox.addActionListener(action -> {
            ServerWrecker.getInstance().setServiceServer((ServiceServer) serviceBox.getSelectedItem());
            ServerWrecker.getLogger().info("Switched auth servers to " + ((ServiceServer) Objects.requireNonNull(serviceBox.getSelectedItem())).getName());
        });

        accounts.add(loadAccounts);
        accounts.add(serviceBox);

        add(accounts);

        JPanel proxies = new JPanel();
        JButton loadProxies = new JButton("Load proxies");
        JFileChooser proxiesChooser = new JFileChooser();

        proxiesChooser.addChoosableFileFilter(new FileNameExtensionFilter("", "txt"));
        loadProxies.addActionListener(new LoadProxiesListener(wireBot, parent, proxiesChooser));

        Arrays.stream(ProxyType.values()).forEach(proxyTypeCombo::addItem);

        proxyTypeCombo.setSelectedItem(ProxyType.SOCKS5);

        proxies.add(loadProxies);
        proxies.add(proxyTypeCombo);

        proxies.add(new JLabel("Accounts per proxy: "));
        accPerProxy.setValue(-1);
        proxies.add(accPerProxy);

        add(proxies);
    }

    @Override
    public String getNavigationName() {
        return "Accounts";
    }

    @Override
    public String getRightPanelContainerConstant() {
        return RightPanelContainer.ACCOUNT_MENU;
    }
}