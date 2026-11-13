import { access, readFile, readdir } from 'node:fs/promises';
import { resolve } from 'node:path';
import { pathToFileURL } from 'node:url';
import { chromium } from 'playwright-core';

const fonte = await readFile('apresentacao.md', 'utf8');
const revisao = await readFile('revisao.md', 'utf8');
const ids = [...fonte.matchAll(/^<!-- slide-id: ([a-z0-9-]+) -->$/gm)].map(([, id]) => id);
const revisados = [...revisao.matchAll(/^## slide-id: ([a-z0-9-]+)$/gm)].map(([, id]) => id);
const titulos = [...fonte.matchAll(/^#{1,2} (.+)$/gm)].map(([, titulo]) => titulo);
titulos.splice(1, 1); // A capa possui título e subtítulo.
if (!fonte.startsWith('---\nmarp: true\n') || ids.length !== titulos.length ||
    new Set(ids).size !== ids.length || ids.join(',') !== revisados.join(',')) {
  throw new Error('A fonte, os identificadores e a revisão devem corresponder slide a slide.');
}

async function conferirLinks(pasta) {
  for (const entrada of await readdir(pasta, { withFileTypes: true })) {
    if (entrada.name.startsWith('.') || ['local', 'target', 'node_modules', 'evidencias'].includes(entrada.name)) continue;
    const arquivo = resolve(pasta, entrada.name);
    if (entrada.isDirectory()) await conferirLinks(arquivo);
    if (!entrada.isFile() || !entrada.name.endsWith('.md')) continue;
    const markdown = await readFile(arquivo, 'utf8');
    for (const [, destino] of markdown.matchAll(/\[[^\]\n]+\]\(([^)\s]+)\)/g)) {
      if (/^[a-z]+:|^#/i.test(destino)) continue;
      const caminho = resolve(pasta, decodeURIComponent(destino.split('#')[0]));
      await access(caminho).catch(() => { throw new Error(`Link quebrado em ${arquivo}: ${destino}`); });
    }
  }
}

await conferirLinks('.');

const navegador = await chromium.launch({ args: ['--no-sandbox'] });
const problemas = [];
try {
  const contexto = await navegador.newContext({ viewport: { width: 1280, height: 720 }, offline: true });
  const pagina = await contexto.newPage();
  pagina.on('pageerror', (erro) => problemas.push(erro.message));
  await contexto.route(/^https?:/, async (rota) => {
    problemas.push(`Recurso remoto: ${rota.request().url()}`);
    await rota.abort();
  });
  await pagina.goto(pathToFileURL(resolve('index.html')).href);
  const slides = pagina.locator('svg[data-marpit-svg] > foreignObject > section[id]');
  if (await slides.count() !== ids.length || await pagina.locator('.bespoke-marp-note').count() !== ids.length) {
    throw new Error('O HTML deve conter um slide e uma nota por identificador.');
  }
  if (!await pagina.locator('table').count() || !await pagina.locator('pre code, marp-pre code').count()) {
    throw new Error('Tabelas e códigos devem estar presentes.');
  }

  for (const [indice, id] of ids.entries()) {
    await pagina.evaluate((n) => { location.hash = String(n); }, indice + 1);
    const slide = slides.nth(indice);
    await slide.locator('h1, h2').first().waitFor({ state: 'visible' });
    const falhas = await slide.evaluate(async (elemento, titulo) => {
      await document.fonts.ready;
      const erros = [];
      if (elemento.querySelector('h1, h2').textContent.trim() !== titulo) erros.push('Título divergente ou acentuação incorreta');
      const caixa = elemento.getBoundingClientRect();
      const escala = caixa.width / elemento.clientWidth;
      const rodape = elemento.querySelector('footer')?.getBoundingClientRect().top ?? caixa.bottom;
      const fonte = elemento.querySelector('.source');
      const limite = fonte ? Math.min(rodape, fonte.getBoundingClientRect().top) : rodape;
      for (const el of Array.from(elemento.children)) {
        if (['FOOTER', 'HEADER'].includes(el.tagName) || el === fonte) continue;
        const retangulo = el.getBoundingClientRect();
        if (retangulo.bottom > limite - 4 * escala || retangulo.right > caixa.right + 1 || retangulo.left < caixa.left - 1) {
          erros.push(`Conteúdo fora da área em ${el.tagName} (fim ${Math.round(retangulo.bottom)}, limite ${Math.round(limite)})`);
        }
      }
      for (const el of elemento.querySelectorAll('pre, marp-pre, table')) {
        if (el.scrollWidth > el.clientWidth + 2) erros.push(`Largura excedida em ${el.tagName}`);
      }
      for (const imagem of elemento.querySelectorAll('img')) {
        try { await imagem.decode(); } catch { erros.push('Diagrama indisponível'); }
      }
      return erros;
    }, titulos[indice]);
    problemas.push(...falhas.map((falha) => `${indice + 1} ${id}: ${falha}`));
  }
  if (await slides.locator('img').count() !== 4) problemas.push('Os quatro diagramas devem estar no HTML.');

  await pagina.evaluate(() => { location.hash = '1'; });
  await pagina.waitForURL(/#1$/);
  await pagina.keyboard.press('ArrowRight');
  await pagina.waitForURL(/#2$/);
  const [apresentador] = await Promise.all([pagina.waitForEvent('popup'), pagina.keyboard.press('p')]);
  await apresentador.locator('.bespoke-marp-note[data-index="1"]').waitFor({ state: 'visible' });
  await apresentador.close();

  if (problemas.length) throw new Error(problemas.join('\n'));
  console.log(`${ids.length} slides e notas verificados no Chromium offline. Tabelas, código, quatro diagramas e navegação disponíveis.`);
} finally {
  await navegador.close();
}
