package com.yby.api.controller;

import com.yby.api.entity.Alerta;
import com.yby.api.entity.Desmatamento;
import com.yby.api.entity.EmissaoCarbono;
import com.yby.api.entity.Indicador;
import com.yby.api.entity.Municipio;
import com.yby.api.entity.Usuario;
import com.yby.api.entity.enums.AlertaTipo;
import com.yby.api.entity.enums.Bioma;
import com.yby.api.entity.enums.FonteDesmatamento;
import com.yby.api.entity.enums.Gravidade;
import com.yby.api.entity.enums.Role;
import com.yby.api.entity.enums.Semaforo;
import com.yby.api.repository.AlertaRepository;
import com.yby.api.repository.DesmatamentoRepository;
import com.yby.api.repository.EmissaoCarbonoRepository;
import com.yby.api.repository.IndicadorRepository;
import com.yby.api.repository.MunicipioRepository;
import com.yby.api.repository.UsuarioRepository;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import com.yby.api.service.IbgeSyncService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * Base para os testes de integracao de endpoints (web layer + persistencia em H2).
 *
 * <p>Cada teste roda dentro de uma transacao revertida ao final ({@code @Transactional}),
 * partindo de um banco limpo recriado pelo Hibernate ({@code ddl-auto=create-drop}). O
 * {@link #setUpBase()} semeia um GESTOR, um SERVIDOR e um municipio base reutilizados pelos
 * testes. O {@link IbgeSyncService} e mockado para nao realizar chamadas de rede.</p>
 */
@SpringBootTest
@Transactional
abstract class AbstractApiIntegrationTest {

    protected static final String GESTOR_EMAIL = "gestor.teste@yby.local";
    protected static final String SERVIDOR_EMAIL = "servidor.teste@yby.local";
    protected static final String SENHA_PADRAO = "senha12345";

    @Autowired protected WebApplicationContext context;
    protected MockMvc mockMvc;
    @Autowired protected UsuarioRepository usuarioRepository;
    @Autowired protected MunicipioRepository municipioRepository;
    @Autowired protected IndicadorRepository indicadorRepository;
    @Autowired protected AlertaRepository alertaRepository;
    @Autowired protected DesmatamentoRepository desmatamentoRepository;
    @Autowired protected EmissaoCarbonoRepository emissaoCarbonoRepository;
    @Autowired protected PasswordEncoder passwordEncoder;

    /** Mockado para evitar chamada de rede ao IBGE nos testes do endpoint de sync. */
    @MockitoBean protected IbgeSyncService ibgeSyncService;

    protected Usuario gestor;
    protected Municipio municipio;

    @BeforeEach
    void setUpBase() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
        gestor = novoUsuario("Gestor Teste", GESTOR_EMAIL, Role.GESTOR);
        novoUsuario("Servidor Teste", SERVIDOR_EMAIL, Role.SERVIDOR);
        municipio = novoMunicipio("Palmas Teste", "1721000", new BigDecimal("80.00"), Semaforo.VERDE);
    }

    protected Usuario novoUsuario(String nome, String email, Role role) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(SENHA_PADRAO));
        usuario.setRole(role);
        usuario.setAtivo(true);
        usuario.setPrimeiroAcessoTrocaSenha(false);
        return usuarioRepository.save(usuario);
    }

    protected Municipio novoMunicipio(String nome, String codigoIbge, BigDecimal score, Semaforo semaforo) {
        Municipio m = new Municipio();
        m.setNome(nome);
        m.setCodigoIbge(codigoIbge);
        m.setScorePrioridade(score);
        m.setSemaforo(semaforo);
        m.setBioma(Bioma.CERRADO);
        m.setAreaHa(new BigDecimal("100000.00"));
        return municipioRepository.save(m);
    }

    protected Indicador novoIndicador(Municipio m, int ano, BigDecimal gasto, BigDecimal resultado) {
        Indicador i = new Indicador();
        i.setMunicipio(m);
        i.setAno(ano);
        i.setGastoPublico(gasto);
        i.setResultadoAmbiental(resultado);
        return indicadorRepository.save(i);
    }

    protected EmissaoCarbono novaEmissao(Municipio m, LocalDate data, BigDecimal valor) {
        EmissaoCarbono e = new EmissaoCarbono();
        e.setMunicipio(m);
        e.setDataReferencia(data);
        e.setEmissaoTco2e(valor);
        e.setFonte("SEEG");
        return emissaoCarbonoRepository.save(e);
    }

    protected Desmatamento novoDesmatamento(Municipio m, LocalDate data, BigDecimal area, FonteDesmatamento fonte) {
        Desmatamento d = new Desmatamento();
        d.setMunicipio(m);
        d.setDataReferencia(data);
        d.setAreaHa(area);
        d.setFonte(fonte);
        d.setBioma("CERRADO");
        return desmatamentoRepository.save(d);
    }

    protected Alerta novoAlerta(Municipio m, AlertaTipo tipo, Gravidade gravidade) {
        Alerta a = new Alerta();
        a.setMunicipio(m);
        a.setTipo(tipo);
        a.setGravidade(gravidade);
        a.setDescricao("Alerta de teste " + tipo);
        a.setAcaoRecomendada("Acao recomendada de teste");
        a.setDataAlerta(LocalDate.now());
        a.setAtivo(true);
        return alertaRepository.save(a);
    }
}
