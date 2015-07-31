package io.vexor.cloud.models

import io.vexor.cloud.TestAppEnv
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class CASpec extends WordSpecLike with Matchers with BeforeAndAfterAll with TestAppEnv {
  val _db     = new DB(dbUrl)
  val db      = new PropertiesTable(_db.open().get, "properties_ca")

  override def beforeAll() : Unit = {
    db.down()
    db.up()
  }

  override def afterAll() : Unit = {
    db.down()
    _db.close()
  }

  "A CA" must {
    "successfuly load, generate and save root certificates" in {
      val ca = CA("id", "subject", db)
      assert(ca.cert.getIssuerDN.toString == "CN=cloud.vexor.io")
      assert(ca.cert.getSubjectDN.toString == "CN=subject")

      Thread.sleep(100)

      val nextCa = CA("id", "subject", db)
      assert(ca.cert.getSerialNumber == nextCa.cert.getSerialNumber)
    }
  }
}

