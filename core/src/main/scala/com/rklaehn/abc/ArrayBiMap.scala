package com.rklaehn.abc

import scala.{ specialized => sp }

class ArrayBiMap[@sp(Int, Long, Double) K, @sp(Int, Long, Double) V] private[abc] (
  val kv: ArrayMap[K, V],
  val vk: ArrayMap[V, K]) {

  def swap: ArrayBiMap[V, K] = new ArrayBiMap[V, K](vk, kv)

  def merge(that: ArrayBiMap[K, V])(implicit kArrayTag: OrderedArrayTag[K], vArrayTag: OrderedArrayTag[V]): ArrayBiMap[K, V] =
    new ArrayBiMap(
      kv.merge(that.kv),
      vk.merge(that.vk))

  def exceptKeys(keys: ArraySet[K])(implicit kArrayTag: OrderedArrayTag[K], vArrayTag: OrderedArrayTag[V]): ArrayBiMap[K, V] = {
    val removedKeys = keys intersect kv.keys
    val kv1 = kv.exceptKeys(removedKeys)
    val values = removedKeys.elements.map(kv.apply)
    val vk1 = vk.exceptKeys(ArraySet(values: _*))
    new ArrayBiMap[K, V](kv1, vk1)
  }

  override def equals(that: Any) = that match {
    case that: ArrayBiMap[K, V] => this.kv == that.kv
    case _ => false
  }

  override def hashCode =
    kv.hashCode

  override def toString =
    s"ArrayBiMap($kv)"
}

object ArrayBiMap {

  def empty[@sp(Int, Long, Double) K, @sp(Int, Long, Double) V](
    implicit kArrayTag: OrderedArrayTag[K], vArrayTag: OrderedArrayTag[V]): ArrayBiMap[K, V] =
    new ArrayBiMap[K, V](ArrayMap.empty[K,V], ArrayMap.empty[V, K])

  def singleton[@sp(Int, Long, Double) K, @sp(Int, Long, Double) V](k: K, v: V)(
    implicit kArrayTag: OrderedArrayTag[K], vArrayTag: OrderedArrayTag[V]): ArrayBiMap[K, V] =
    new ArrayBiMap[K, V](
      ArrayMap.singleton[K, V](k, v),
      ArrayMap.singleton[V, K](v, k))

  def apply[@sp(Int, Long, Double) K, @sp(Int, Long, Double) V](kvs: (K, V)*)(
    implicit kArrayTag: OrderedArrayTag[K], vArrayTag: OrderedArrayTag[V]): ArrayBiMap[K, V] = {
    new ArrayBiMap[K, V](
      ArrayMap(kvs: _*),
      ArrayMap(kvs.map(_.swap): _*))
  }
}
