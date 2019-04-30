
		<div id="ygor-messages">
			<g:if test="${flash.info}">
        <div class="panel-group">
          <div class="panel panel-default">
            <div class="panel-heading">
              <h4 class="panel-title">
                <a data-toggle="collapse" href="#gokb-info">GOKb Infos</a>
              </h4>
            </div>
            <div id="gokb-info" class="panel-collapse collapse in">
              <div class="panel-body">
                <div class="alert alert-info" role="alert" style="z-index:2;">
                  ${flash.info}
                </div>
              </div>
            </div>
          </div>
        </div>
			</g:if>
			<g:if test="${flash.error}">
        <div class="panel-group">
          <div class="panel panel-default">
            <div class="panel-heading">
              <h4 class="panel-title">
                <a data-toggle="collapse" href="#gokb-errors">GOKb Infos</a>
              </h4>
            </div>
            <div id="gokb-errors" class="panel-collapse collapse in">
              <div class="panel-body">
                <g:if test="${flash.error instanceof String}">
                  <div class="alert alert-danger" role="alert" style="z-index:2;">
                    <g:message message="${flash.error}" args="${flash.args}" default="${flash.default}"/>
                  </div>
                </g:if>
                <g:else>
                  <g:each var="m" in="${flash.error}">
                    <div class="alert alert-danger" role="alert" style="z-index:2;">
                      ${m}
                    </div>
                  </g:each>
                </g:else>
              </div>
            </div>
          </div>
        </div>
			</g:if>
			<g:if test="${flash.warning}">
        <div class="panel-group">
          <div class="panel panel-default">
            <div class="panel-heading">
              <h4 class="panel-title">
                <a data-toggle="collapse" href="#gokb-warnings">GOKb Infos</a>
              </h4>
            </div>
            <div id="gokb-warnings" class="panel-collapse collapse in">
              <div class="panel-body">
                <g:if test="${flash.warning instanceof String}">
                  <div class="alert alert-warning" role="alert" style="z-index:2;">
                    <g:message message="${flash.warning}" args="${flash.args}" default="${flash.default}"/>
                  </div>
                </g:if>
                <g:else>
                  <g:each var="m" in="${flash.warning}">
                    <div class="alert alert-warning" role="alert" style="z-index:2;">
                      ${m}
                    </div>
                  </g:each>
                </g:else>
              </div>
            </div>
          </div>
        </div>
			</g:if>
		</div>
