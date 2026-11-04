const localFormat = new Intl.DateTimeFormat('pt-BR', {
    weekday: 'short',
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
})

const utcFormat = new Intl.DateTimeFormat('en-GB', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    timeZone: 'UTC',
})

// The API always sends UTC instants; only the screen converts them.
export function formatLocal(iso: string): string {
    return localFormat.format(new Date(iso))
}

export function formatUtc(iso: string): string {
    return `${utcFormat.format(new Date(iso))} UTC`
}

export function formatDuration(seconds: number): string {
    const total = Math.round(seconds)
    const minutes = Math.floor(total / 60)
    const rest = total % 60
    return `${minutes} min ${String(rest).padStart(2, '0')} s`
}