package cm.aptoide.pt.networkclient.okhttp.newCache;

/**
 * Created by sithengineer on 28/10/2016.
 */

abstract class StringBaseCache<K, V> extends Cache<K, V, String> {

  public StringBaseCache(KeyAlgorithm<K, String> keyAlgorithm) {
    super(keyAlgorithm);
  }

  @Override public void put(K key, V value) {
    if (keyAlgorithm == null) {
      throw new UnsupportedOperationException("Initialize cache using init() first");
    }
    put(keyAlgorithm.getKeyFrom(key), value);
  }

  abstract void put(String key, V value);

  @Override public V get(K key) {
    if (keyAlgorithm == null) {
      throw new UnsupportedOperationException("Initialize cache using init() first");
    }
    if(isValid(key)){
      return get(keyAlgorithm.getKeyFrom(key));
    }
    return null;
  }

  abstract V get(String key);

  @Override public boolean contains(K key) {
    if (keyAlgorithm == null) {
      throw new UnsupportedOperationException("Initialize cache using init() first");
    }
    return contains(keyAlgorithm.getKeyFrom(key));
  }

  abstract boolean contains(String key);

  @Override public boolean isValid(K key) {
    if (keyAlgorithm == null) {
      throw new UnsupportedOperationException("Initialize cache using init() first");
    }
    String keyAsString = keyAlgorithm.getKeyFrom(key);
    if(contains(keyAsString)) {
      return isValid(keyAsString);
    }
    return false;
  }

  abstract boolean isValid(String key);
}
