package icewizard7.miningServerPlugin.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public class VoucherManager {
    private final NamespacedKey voucherKey;
    private final NamespacedKey fragmentKey;

    public VoucherManager(Plugin plugin) {
        this.voucherKey = new NamespacedKey(plugin, "voucher");
        this.fragmentKey = new NamespacedKey(plugin, "fragment");
    }

    public NamespacedKey getVoucherKey() {
        return voucherKey;
    }

    public NamespacedKey getFragmentKey() {
        return fragmentKey;
    }
}
