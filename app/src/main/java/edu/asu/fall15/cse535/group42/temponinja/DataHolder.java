package edu.asu.fall15.cse535.group42.temponinja;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Created by royster on 10/22/15.
 */
public class DataHolder {

	// TAG information
	private static final String TAG = "DataHolder";





	///////////////////////////////////////////
	// Data holder using Singleton class

	// Private constructor. Prevents instantiation from other classes.
	private DataHolder() {
		Log.i(TAG, "DataHolder constructor");
		// The directory is initialized here
		File dir = new File (path);
		if (!dir.exists()) {
			dir.mkdir();
		}
	}
	/**
	 * Initializes singleton.
	 *
	 * @see <a href="https://en.wikipedia.org/wiki/Singleton_pattern#Initialization-on-demand_holder_idiom">Wikipedia: Initialization-on-demand by Dr. Bill Pugh</a>
	 * {@link DataHolderHolder} is loaded on the first execution of {@link DataHolder#getInstance()} or the first access to
	 * {@link DataHolderHolder#INSTANCE}, not before.
	 */
	private static class DataHolderHolder {
		private static final DataHolder INSTANCE = new DataHolder();
	}
	public static DataHolder getInstance() {
		return DataHolderHolder.INSTANCE;
	}







	///////////////////////////////////////////
	// Shared data is here
	Map<File, ClipProperty>		musicMap;				// map music file to ClipProperty
	File						curMusicTempoTask = null;




	///////////////////////////////////////////
	// TempoInfoFile
	public static final File	sdcard = Environment.getExternalStorageDirectory();
	public static final String	subdir = "TempoNinja";
	public static final String 	path = sdcard.getAbsolutePath() + "/" + subdir + "/";
	public static final String	tempoInfoFileName 		= "tempo.xml";
	public static final String	tmpTempoInfoFileName 	= "tempoTmp.xml";
	public static final String	oldTempoInfoFileName 	= "tempoBak.xml";
	private static final String xmlRootTag 				= "TempoInfoList";
	private static final String xmlElementTag 			= "TempoInfo";
	public void readTempoInfo () {
		Log.i(TAG, "readTempoInfo");

		Map<String, TempoInfo>		tmpTempoInfoMap = new HashMap<>();
		// Main procedure for reading the xml
		try {
			// Read xml string into DOM
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new File(path + tempoInfoFileName));

			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			// Get the list of TempoInfos
			NodeList nList = doc.getElementsByTagName(xmlElementTag);

			// Read each element into a TempoInfo
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;

					// Read HashString
					String hashStr = eElement.getAttribute("Tag");

					// Read TempoInfo
					TempoInfo tempoInfo = new TempoInfo();
					tempoInfo.tempo = Double.parseDouble(eElement.getElementsByTagName("Tempo").item(0).getTextContent());            // Tempo
					tempoInfo.anchor = Double.parseDouble(eElement.getElementsByTagName("Anchor").item(0).getTextContent());        // Anchor
					tempoInfo.version = Long.parseLong(eElement.getElementsByTagName("Version").item(0).getTextContent());            // Version

					// Update the map
					tmpTempoInfoMap.put(hashStr, tempoInfo);
				}
			}

			Log.i(TAG, "tmpTempoInfoMap: " + tmpTempoInfoMap.toString());

			// After import, update
			if (tempoInfoMap == null) {
				tempoInfoMap = tmpTempoInfoMap;
			} else {
				tempoInfoMap.putAll(tmpTempoInfoMap);
			}
		} catch (Exception e) {
			Log.i(TAG, "[X] Exception: " + e.toString());
		}
	}
	public void writeTempoInfo () {
		Log.i(TAG, "writeTempoInfo");

		// Main procedure for reading the xml
		try {
			// Create DOM object
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.newDocument();

			// Append main root elements
			Element mainRootElement = doc.createElementNS(TAG, xmlRootTag);
			doc.appendChild(mainRootElement);

			// Append the items
			for (String hash : tempoInfoMap.keySet()) {
				TempoInfo tempoInfo = tempoInfoMap.get(hash);
				Element tempoInfoNode = doc.createElement(xmlElementTag);
				tempoInfoNode.setAttribute("Tag", hash);

				// Tempo node
				Element tempo = doc.createElement("Tempo");
				tempo.appendChild(doc.createTextNode("" + tempoInfo.getTempo()));

				// Anchor node
				Element anchor = doc.createElement("Anchor");
				anchor.appendChild(doc.createTextNode(""+tempoInfo.getAnchor()));

				// Version node
				Element version = doc.createElement("Version");
				version.appendChild(doc.createTextNode(""+tempoInfo.getVersion()));

				// Append these nodes
				tempoInfoNode.appendChild(tempo);
				tempoInfoNode.appendChild(anchor);
				tempoInfoNode.appendChild(version);

				// Add to root
				mainRootElement.appendChild(tempoInfoNode);
			}

			// Output DOM XML to file
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(doc);
			File outFile = new File (path + tmpTempoInfoFileName);
			StreamResult outStream = new StreamResult(outFile);
			transformer.transform(source, outStream);

			// Rename the files
			File oldFile = new File (path + tempoInfoFileName);
			if (oldFile.exists()) {
				oldFile.renameTo(new File (path + oldTempoInfoFileName));
			}
			File newFile = new File (path + tmpTempoInfoFileName);
			newFile.renameTo(oldFile);
		} catch (Exception e) {
			Log.i(TAG, "[X] Exception: " + e.toString());
		}
	}



	///////////////////////////////////////////
	// Tempo groups
	Map<String, TempoInfo>		tempoInfoMap;					// map string (from ClipProperty.getHash()) to its tempo information
	public static final long [] tempoGroups = {90, 95, 100, 105, 110, 115, 120, 130, 140, 150, 155, 160, 165, 170, 175, 180, 185, 190, 195, 200, 210, 220, 230, 1000};  // Group tempos into groups
	List< List >				tempoGroupMusicList;			// list of music in each tempo group
	/**
	 * Find the index of tempo group that a tempo belongs to.
	 * @param tempo
	 * @return index of the tempo group (lower bounding tempo) in tempoGroups
	 */
	public static int 			findTempoGroup (long tempo) {
		int idx = 0;
		while (idx < tempoGroups.length && tempoGroups[idx] < tempo) {
			idx++;
		}
		return idx;
	}






	///////////////////////////////////////////
	// Setters and getters
	void 						setMusicMap (Map<File, ClipProperty> i_musicMap) 			{ musicMap = i_musicMap; }
	void 						setTempoInfoMap (Map<String, TempoInfo> i_tempoInfoMap) 	{ tempoInfoMap = i_tempoInfoMap; }
	void 						setCurMusicTempoTask (File i_curMusicTempoTask) 			{ curMusicTempoTask = i_curMusicTempoTask; }
	void 						setTempoGroupMusicList (List< List > i_tempoGroupMusicList)	{ tempoGroupMusicList = i_tempoGroupMusicList; }
	Map<File, ClipProperty> 	getMusicMap () 												{ return musicMap; }
	Map<String, TempoInfo> 		getTempoInfoMap () 											{ return tempoInfoMap; }
	File 						getCurMusicTempoTask () 									{ return curMusicTempoTask; }
	List< List > 				getTempoGroupMusicList ()									{ return tempoGroupMusicList; }




	///////////////////////////////////////////
	// DataHolder initialization
	public void init() {
		Log.i(TAG, "init");

		// Read tempo info from file
		readTempoInfo();

		// Obtain list of all music files
		updateMusicList();

		// TODO: Open the file to write new tempo info
	}
	///////////////////////////////////////////
	// DataHolder update
	public void update() {
		Log.i(TAG, "update");

		// Obtain list of all music files
		updateMusicList();
	}




	///////////////////////////////////////////
	// Used to update music files
	private List<File> getMusicList () {
		return getMusicList(Environment.getExternalStorageDirectory());
	}
	private List<File> getMusicList (File root) {
		List<File> lst = new ArrayList<> ();
		File[] files = root.listFiles();
		if (files != null) {
			Arrays.sort(files);
			for (File file : files) {
				if (file.isDirectory() && !file.isHidden()) {
					lst.addAll(getMusicList(file));
				} else {
					if (file.getName().endsWith(".mp3")) {
						lst.add(file);
					}
				}
			}
		}
		return lst;
	}



	///////////////////////////////////////////
	// Update music list (can also used as initialization)
	private void updateMusicList () {
		Log.i(TAG, "updateMusicList");

		// Instantiate collections

		if (tempoGroupMusicList == null) tempoGroupMusicList = new ArrayList<List> ();
		if (musicMap == null) musicMap = new HashMap<> ();
		if (tempoInfoMap == null) tempoInfoMap = new HashMap<> ();

		// Initialize musicTempoMap
		for (int i = 0; i < tempoGroups.length+1; i++) {
			List<File> fileList = new ArrayList<> ();
			tempoGroupMusicList.add(fileList);
		}

		// Obtain music list
		musicMap.clear();
		List<File> musicList = getMusicList();	 // Get list of all music from the sdcard
		if (!musicList.isEmpty()) {
			for (File musicFile : musicList) {
				if (!musicMap.containsKey(musicFile)) {		// File object based. Don't know if this works.
					Log.i(TAG, "File: " + musicFile.toString());
					ClipProperty cp = ClipReaderAndroid.readClip(musicFile.getPath());
					Log.i(TAG, "Hash: " + cp.getHash());
					musicMap.put(musicFile, cp);

					String hash = cp.getHash();
					if (!tempoInfoMap.containsKey(hash)) {
						// Add to newMusicList (deprecated)
					} else {
						// Update tempoGroupMusicList
						TempoInfo tempo = tempoInfoMap.get(hash);
						int tempoGroupIdx = findTempoGroup((long) tempo.getTempo());

						tempoGroupMusicList.get(tempoGroupIdx).add(musicFile);
					}
				}
			}
		}

		Log.i(TAG, "updateMusicList finished");
	}



	///////////////////////////////////////////
	// Insert/update tempoInfoMap
	public void updateTempoInfoMap (File musicFile, TempoInfo tempoInfo) {
		// Store the old tempo
		ClipProperty cp = musicMap.get(musicFile);
		String hash = cp.getHash();
		TempoInfo oldTempoInfo = null;
		if (tempoInfoMap.containsKey(hash)) {
			oldTempoInfo = tempoInfoMap.get(hash);
		}

		// Update the Tempo Info Map
		tempoInfoMap.put(hash, tempoInfo);
		writeTempoInfo();

		// Regroup the music
		int newGroup = findTempoGroup((long) tempoInfo.getTempo());
		if (oldTempoInfo != null) {
			int oldGroup = findTempoGroup((long) oldTempoInfo.getTempo());
			if (newGroup == oldGroup) return;
			else if (tempoGroupMusicList.get(oldGroup).contains(musicFile)) {
				tempoGroupMusicList.get(oldGroup).remove(musicFile);
			}
		}
		tempoGroupMusicList.get(newGroup).add(musicFile);

		Log.i(TAG, "Groups: " + tempoGroupMusicList.toString());
	}
}
