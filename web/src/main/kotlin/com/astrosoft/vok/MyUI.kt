package com.astrosoft.vok

import com.astrosoft.vok.views.*
import com.astrosoft.vok.views.usuarioView.UsuarioView
import com.github.vok.karibudsl.autoViewProvider
import com.github.vok.karibudsl.item
import com.github.vok.karibudsl.valoMenu
import com.vaadin.annotations.*
import com.vaadin.icons.VaadinIcons
import com.vaadin.navigator.Navigator
import com.vaadin.navigator.PushStateNavigation
import com.vaadin.navigator.ViewDisplay
import com.vaadin.server.ClassResource
import com.vaadin.server.Page
import com.vaadin.server.VaadinRequest
import com.vaadin.shared.Position
import com.vaadin.shared.ui.ui.Transport
import com.vaadin.ui.Notification
import com.vaadin.ui.UI
import com.vaadin.ui.themes.ValoTheme
import org.slf4j.LoggerFactory

/**
 * The Vaadin UI which demoes all the features. If not familiar with Vaadin, please check out the Vaadin tutorial first.
 * @author mvy
 */
@Theme("mytheme")
@Title("Vaadin On Kotlin")
@Push(transport = Transport.WEBSOCKET_XHR)
@Viewport("width=device-width, initial-scale=1.0")
@JavaScript("https://code.jquery.com/jquery-2.1.4.min.js",
            "https://code.responsivevoice.org/responsivevoice.js")
@PushStateNavigation
class MyUI : UI() {

  private val content = valoMenu {
    appTitle = "<h3>Karibu-DSL <strong>Sample App</strong></h3>"
    userMenu {
      item("John Doe", ClassResource("profilepic300px.jpg")) {
        item("Edit Profile")
        item("Preferences")
        addSeparator()
        item("Sign Out")
      }

      menuButton("Tela Principal", VaadinIcons.PIE_BAR_CHART, view = TelaPrincipalView::class.java)

      section("Segurança")
      menuButton("Usuários", VaadinIcons.USER, view = UsuarioView::class.java)
      menuButton("Grupos", VaadinIcons.USERS, view = GrupoView::class.java)

      section("Movimentção")
      menuButton("Recebimento de Notas", VaadinIcons.TRUCK, view = RecebimentoNotaView::class.java)
      menuButton("Endereçamento", VaadinIcons.LOCATION_ARROW_CIRCLE_O, view = EnderecamentoView::class.java)
      menuButton("Picking", VaadinIcons.ARROW_CIRCLE_DOWN, view = PikingView::class.java)
      menuButton("Ordem de serviço", VaadinIcons.ARROW_CIRCLE_DOWN, view = OrdemServicoView::class.java)

      section("Consulta")
      menuButton("NF de Entrada", VaadinIcons.INBOX, view = NFEntradaView::class.java)
      menuButton("Mapa de Depósito", VaadinIcons.PACKAGE, view = MapaDepositoView::class.java)
      menuButton("Consulta Produtos", VaadinIcons.CUBE, view = ConsultaProdutoView::class.java)
      menuButton("Consulta Endereços", VaadinIcons.LOCATION_ARROW_CIRCLE_O, view = ConsultaEnderecoView::class.java)
    }
  }

  override fun init(request: VaadinRequest?) {
    setContent(content)
    navigator = Navigator(this, content as ViewDisplay)
    navigator.addProvider(autoViewProvider)
    setErrorHandler { e ->
      log.error("Vaadin UI uncaught exception ${e.throwable}", e.throwable)
      val description = "Ocorreu um erro, e nos sentimos muito por isso. Já está trabalhando na solução!"
      // when the exception occurs, show a nice notification
      Notification("Oops", description, Notification.Type.ERROR_MESSAGE).apply {
        styleName = "${ValoTheme.NOTIFICATION_CLOSABLE} ${ValoTheme.NOTIFICATION_ERROR}"
        position = Position.TOP_CENTER
        show(Page.getCurrent())
      }
    }
  }

  companion object {
    @JvmStatic
    private val log = LoggerFactory.getLogger(MyUI::class.java)
  }
}




