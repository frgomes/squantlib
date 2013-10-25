package squantlib.model.asset

import squantlib.util.Date
import squantlib.math.timeseries.TimeSeries
import org.jquantlib.currencies.Currency

/**
 * StaticAsset with currency & analysis features
 */
trait AnalyzedAsset extends StaticAsset {
  
  val currency:Currency
  
  def latestPriceLocalCcy:Option[Double]  // to be implemented in subclass
  
  def assetStartDate:Option[Date] = None // to be implemented in subclass
  
  def assetEndDate:Option[Date] = None // to be implemented in subclass
  
  lazy val firstData = priceHistory.firstDate

  lazy val lastData = priceHistory.lastDate
  
  lazy val priceStartDate:Date = assetStartDate.collect{case d => if (d ge firstData) d else firstData}.getOrElse(firstData)
  
  lazy val priceEndDate:Date = assetEndDate.collect{case d => if (d le lastData) d else lastData}.getOrElse(lastData)
  
  def isPricedOn(d:Date):Boolean = (d ge priceStartDate) && (d le priceEndDate)
  
  def isPriced:Boolean
  
  def getLivePriceHistory:TimeSeries = TimeSeries(priceHistory.filterKeys(d => (d ge priceStartDate) && (d le priceEndDate)))
  
} 

