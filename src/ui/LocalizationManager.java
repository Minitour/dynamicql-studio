package ui;

import java.util.*;

public final class LocalizationManager {

    /**
     * Singleton
     */
    private static LocalizationManager manager;

    /**
     * @return Get singleton instance
     */
    public static LocalizationManager shared(){
        return manager == null ? manager = new LocalizationManager() : manager;
    }

    /**
     * Path for localization files
     */
    private String bundlePath = null;//"resources.values.strings";

    /**
     * Current language code
     */
    private String lang;

    /**
     * Current Resource Bundle
     */
    private ResourceBundle bundle;

    /**
     * Current views.
     */
    private Map<String,Translatable> translatables;

    /**
     * Private constructor
     */
    private LocalizationManager(){
        translatables = new HashMap<>();
    }

    /**
     * Assign a new translatable object to the localization manager with a key.
     *
     * @param id The unique ID of the translatable object.
     * @param t The translatable object.
     */
    public void register(String id, Translatable t){
        translatables.put(id,t);
    }

    /**
     * Un-assign a translatable object based on it's unique id.
     * @param id The translatable object id.
     */
    public void remove(String id){
        translatables.remove(id);
    }

    /**
     * Notify all registered objects to translate to new localization.
     * @param lang The language code.
     * @throws TranslationException if bundle path has not been set.
     */
    public void translate(String lang){

        if(bundlePath == null)
            throw new TranslationException("Bundle Path is not specified.");

        if(!this.lang.equals(lang) || bundle == null)
            bundle = ResourceBundle.getBundle(bundlePath,new Locale(lang));

        Locale.setDefault(new Locale(lang));

        boolean cleanUpOnFinish = false;

        //notify translation
        for (Translatable t : translatables.values()){
            try {
                t.translate(bundle);
            }catch (NullPointerException e){
                cleanUpOnFinish = true;
            }
        }

        if(cleanUpOnFinish)
            cleanup();

        this.lang = lang;
    }

    public void setBundlePath(String path){
        this.bundlePath = path;
    }

    public ResourceBundle getBundle(){
        return bundle;
    }

    private void cleanup(){

        List<String> keys = new ArrayList<>();
        for (Map.Entry<String,Translatable> entry : translatables.entrySet()){
            if (entry.getValue() == null){
                keys.add(entry.getKey());
            }
        }

        for (String key : keys){
            translatables.remove(key);
        }
    }

    public interface Translatable {
        void translate(ResourceBundle bundle);
    }

    public class TranslationException extends RuntimeException {
        public TranslationException(String message) {
            super(message);
        }
    }
}
