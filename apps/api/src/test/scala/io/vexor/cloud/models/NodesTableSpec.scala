package io.vexor.cloud.models

import java.util.UUID

import io.vexor.cloud.TestAppEnv
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import io.vexor.cloud.models.NodesTable.Status

class NodesTableSpec extends WordSpecLike with Matchers with BeforeAndAfterAll with TestAppEnv {
  val userId  = new UUID(0,0)
  val reg     = ModelRegistry(dbUrl, "NodesTableSpec").get
  val db      = reg.nodes

  override def beforeAll() : Unit = {
    db.down()
    db.up()
  }

  override def afterAll() : Unit = {
    db.down()
    reg.db.close()
  }

  "A NodeTable" must {
    "find all running nodes" in {
      val n0 = NodesTable.New(userId, "n")
      val a0 = NodesTable.New(userId, "a")

      val a1 = db.save(a0).get
      val n1 = db.save(n0).get
      assert(List(n1, a1) == db.allRunning())

      val n2 = db.save(n1, status = Status.Pending).get
      assert(List(a1, n2) == db.allRunning())

      val n3 = db.save(n2, status = Status.Active).get
      assert(List(a1, n3) == db.allRunning())

      val n4 = db.save(n3, status = Status.Finished).get
      assert(List(a1) == db.allRunning())

      val n5 = db.save(n4, status = Status.Broken).get
      assert(List(a1) == db.allRunning())
    }

    "successfuly create and update records" in {
      val role   = "default"
      val nRec = NodesTable.New(userId, role)

      var re  = db.save(nRec)
      re match {
        case Some(NodesTable.Persisted(pUserId, pRole, pVersion, pStatus, pCloudId, _)) =>
          assert(pUserId  == userId)
          assert(pRole    == role)
          assert(pVersion == 1)
          assert(pStatus  == Status.New)
          assert(pCloudId.isEmpty)
        case unknown =>
          fail(s"unknown $unknown")
      }
      val pRec  = re.get
      val pLast = db.last(userId, role).get
      assert(pRec == pLast)

      re = db.save(pRec, status = Status.Pending)
      re match {
        case Some(NodesTable.Persisted(pUserId, pRole, pVersion, pStatus, pCloudId, _)) =>
          assert(pUserId  == userId)
          assert(pRole    == role)
          assert(pVersion == 2)
          assert(pStatus  == Status.Pending)
          assert(pCloudId.isEmpty)
        case unknown =>
          fail(s"unknown $unknown")
      }
      val ppRec = re.get
      val ppLast = db.last(userId, role).get
      assert(ppRec == ppLast)

      re = db.save(ppRec, cloudId = Some("cloudId"))
      re match {
        case Some(NodesTable.Persisted(pUserId, pRole, pVersion, pStatus, pCloudId, _)) =>
          assert(pUserId  == userId)
          assert(pRole    == role)
          assert(pVersion == 3)
          assert(pStatus  == Status.Pending)
          assert(pCloudId.get == "cloudId")
        case unknown =>
          fail(s"unknown $unknown")
      }
      val pppRec = re.get
      val pppLast = db.last(userId, role).get
      assert(pppRec == pppLast)

      val nnRec = NodesTable.New(userId, role)
      re  = db.save(nnRec)
      re match {
        case Some(NodesTable.Persisted(pUserId, pRole, pVersion, pStatus, pCloudId, _)) =>
          assert(pUserId  == userId)
          assert(pRole    == role)
          assert(pVersion == 4)
          assert(pStatus  == Status.New)
          assert(pCloudId.isEmpty)
        case unknown =>
          fail(s"unknown $unknown")
      }
      val ppppRec  = re.get
      val ppppLast = db.last(userId, role).get
      assert(ppppRec == ppppLast)
    }
  }
}

