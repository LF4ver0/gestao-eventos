/**
 *Datas são tratadas como STRING "yyyy-MM-dd" de ponta a ponta (input type="date" -> JSON -> LocalDate).
 */
export function formatarData(iso: string): string {
  const [ano, mes, dia] = iso.split('-');
  return `${dia}/${mes}/${ano}`;
}

/** Data de hoje no fuso local do navegador, em "yyyy-MM-dd" */
export function hojeIso(): string {
  const d = new Date();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${mm}-${dd}`;
}
